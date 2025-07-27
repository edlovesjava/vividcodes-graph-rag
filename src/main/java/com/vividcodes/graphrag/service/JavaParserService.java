package com.vividcodes.graphrag.service;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.vividcodes.graphrag.config.ParserConfig;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;

@Service
public class JavaParserService {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaParserService.class);
    
    private final ParserConfig parserConfig;
    private final GraphService graphService;
    private final JavaParser javaParser;
    
    @Autowired
    public JavaParserService(ParserConfig parserConfig, GraphService graphService) {
        this.parserConfig = parserConfig;
        this.graphService = graphService;
        this.javaParser = new JavaParser();
    }
    
    public void parseDirectory(String sourcePath) {
        try {
            Path rootPath = Paths.get(sourcePath);
            if (!Files.exists(rootPath)) {
                throw new IllegalArgumentException("Source path does not exist: " + sourcePath);
            }
            
            List<Path> javaFiles = findJavaFiles(rootPath);
            logger.info("Found {} Java files to parse", javaFiles.size());
            
            for (Path javaFile : javaFiles) {
                try {
                    parseJavaFile(javaFile);
                } catch (Exception e) {
                    logger.error("Error parsing file: {}", javaFile, e);
                }
            }
            
            logger.info("Completed parsing {} Java files", javaFiles.size());
            
        } catch (Exception e) {
            logger.error("Error parsing directory: {}", sourcePath, e);
            throw new RuntimeException("Failed to parse directory", e);
        }
    }
    
    private List<Path> findJavaFiles(Path rootPath) throws Exception {
        try (Stream<Path> paths = Files.walk(rootPath)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(this::shouldIncludeFile)
                .collect(Collectors.toList());
        }
    }
    
    boolean shouldIncludeFile(Path filePath) {
        if (filePath == null || filePath.getFileName() == null) {
            logger.debug("Skipping null or invalid file path: {}", filePath);
            return false;
        }
        
        String fileName = filePath.getFileName().toString();
        String filePathStr = filePath.toString();
        
        logger.debug("Checking file: {} (fileName: {})", filePath, fileName);
        
        // Check file size
        try {
            if (Files.size(filePath) > parserConfig.getMaxFileSize()) {
                logger.debug("Skipping large file: {}", filePath);
                return false;
            }
        } catch (Exception e) {
            logger.warn("Could not check file size for: {}", filePath, e);
            return false;
        }
        
        // Check if it's a test file
        if (!parserConfig.isIncludeTests() && 
            (fileName.contains("Test") || fileName.contains("test") || 
             filePathStr.contains("/test/") || filePathStr.contains("\\test\\"))) {
            logger.debug("Skipping test file: {} (fileName contains 'Test' or 'test')", filePath);
            return false;
        }
        
        logger.debug("Including file: {}", filePath);
        return true;
    }
    
    private void parseJavaFile(Path filePath) throws FileNotFoundException {
        logger.debug("Parsing file: {}", filePath);
        
        ParseResult<CompilationUnit> parseResult = javaParser.parse(filePath.toFile());
        
        if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
            CompilationUnit cu = parseResult.getResult().get();
            JavaGraphVisitor visitor = new JavaGraphVisitor(filePath.toString());
            visitor.visit(cu, null);
        } else {
            logger.warn("Failed to parse file: {}", filePath);
        }
    }
    
    private class JavaGraphVisitor extends VoidVisitorAdapter<Void> {
        
        private final String filePath;
        private String packageName = "";
        private ClassNode currentClass;
        
        public JavaGraphVisitor(String filePath) {
            this.filePath = filePath;
        }
        
        @Override
        public void visit(PackageDeclaration packageDecl, Void arg) {
            this.packageName = packageDecl.getNameAsString();
            super.visit(packageDecl, arg);
        }
        
        @Override
        public void visit(ClassOrInterfaceDeclaration classDecl, Void arg) {
            // Only process public classes/interfaces
            if (classDecl.isPublic() || parserConfig.isIncludePrivate()) {
                logger.info("Processing class: {}", classDecl.getNameAsString());
                currentClass = createClassNode(classDecl);
                logger.info("Created class node: {}", currentClass.getId());
                graphService.saveClass(currentClass);
                
                // Visit methods and fields
                super.visit(classDecl, arg);
                
                currentClass = null;
            }
        }
        
        @Override
        public void visit(MethodDeclaration methodDecl, Void arg) {
            // Only process public methods
            if (methodDecl.isPublic() || parserConfig.isIncludePrivate()) {
                MethodNode methodNode = createMethodNode(methodDecl);
                graphService.saveMethod(methodNode);
            }
        }
        
        @Override
        public void visit(FieldDeclaration fieldDecl, Void arg) {
            // Only process public fields
            if (fieldDecl.isPublic() || parserConfig.isIncludePrivate()) {
                fieldDecl.getVariables().forEach(variable -> {
                    FieldNode fieldNode = createFieldNode(variable.getNameAsString(), fieldDecl);
                    graphService.saveField(fieldNode);
                });
            }
        }
        
        private ClassNode createClassNode(ClassOrInterfaceDeclaration classDecl) {
            ClassNode classNode = new ClassNode(
                classDecl.getNameAsString(),
                packageName,
                filePath
            );
            
            classNode.setVisibility(getVisibility(classDecl));
            classNode.setModifiers(getModifiers(classDecl));
            classNode.setIsInterface(classDecl.isInterface());
            classNode.setIsEnum(classDecl.isEnumDeclaration());
            classNode.setIsAnnotation(classDecl.isAnnotationDeclaration());
            
            if (classDecl.getBegin().isPresent()) {
                classNode.setLineStart(classDecl.getBegin().get().line);
            }
            if (classDecl.getEnd().isPresent()) {
                classNode.setLineEnd(classDecl.getEnd().get().line);
            }
            
            return classNode;
        }
        
        private MethodNode createMethodNode(MethodDeclaration methodDecl) {
            MethodNode methodNode = new MethodNode(
                methodDecl.getNameAsString(),
                currentClass != null ? currentClass.getName() : "",
                packageName,
                filePath
            );
            
            methodNode.setVisibility(getVisibility(methodDecl));
            methodNode.setModifiers(getModifiers(methodDecl));
            methodNode.setReturnType(methodDecl.getTypeAsString());
            methodNode.setParameters(methodDecl.getParameters().stream()
                .map(param -> param.getTypeAsString())
                .collect(Collectors.toList()));
            methodNode.setParameterNames(methodDecl.getParameters().stream()
                .map(param -> param.getNameAsString())
                .collect(Collectors.toList()));
            
            if (methodDecl.getBegin().isPresent()) {
                methodNode.setLineStart(methodDecl.getBegin().get().line);
            }
            if (methodDecl.getEnd().isPresent()) {
                methodNode.setLineEnd(methodDecl.getEnd().get().line);
            }
            
            return methodNode;
        }
        
        private FieldNode createFieldNode(String fieldName, FieldDeclaration fieldDecl) {
            FieldNode fieldNode = new FieldNode(
                fieldName,
                currentClass != null ? currentClass.getName() : "",
                packageName,
                filePath
            );
            
            fieldNode.setVisibility(getVisibility(fieldDecl));
            fieldNode.setModifiers(getModifiers(fieldDecl));
            fieldNode.setType(fieldDecl.getElementType().asString());
            
            // Find the specific variable's line number
            fieldDecl.getVariables().stream()
                .filter(v -> v.getNameAsString().equals(fieldName))
                .findFirst()
                .ifPresent(v -> {
                    if (v.getBegin().isPresent()) {
                        fieldNode.setLineNumber(v.getBegin().get().line);
                    }
                });
            
            return fieldNode;
        }
        
        private String getVisibility(com.github.javaparser.ast.body.BodyDeclaration<?> declaration) {
            // For now, return a default visibility - we'll enhance this later
            return "PUBLIC";
        }
        
        private List<String> getModifiers(com.github.javaparser.ast.body.BodyDeclaration<?> declaration) {
            // For now, return empty list - we'll enhance this later
            return new ArrayList<>();
        }
    }
} 