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

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaParserService.class);

    private final ParserConfig parserConfig;
    private final GraphService graphService;
    private final JavaParser javaParser;

    @Autowired
    public JavaParserService(final ParserConfig parserConfig, final GraphService graphService) {
        this.parserConfig = parserConfig;
        this.graphService = graphService;
        this.javaParser = new JavaParser();
    }

    public void parseDirectory(final String sourcePath) {
        try {
            Path rootPath = Paths.get(sourcePath);
            if (!Files.exists(rootPath)) {
                throw new IllegalArgumentException("Source path does not exist: " + sourcePath);
            }

            List<Path> javaFiles = findJavaFiles(rootPath);
            LOGGER.info("Found {} Java files to parse", javaFiles.size());

            for (Path javaFile : javaFiles) {
                try {
                    parseJavaFile(javaFile);
                } catch (Exception e) {
                    LOGGER.error("Error parsing file: {}", javaFile, e);
                }
            }

            LOGGER.info("Completed parsing {} Java files", javaFiles.size());

        } catch (Exception e) {
            LOGGER.error("Error parsing directory: {}", sourcePath, e);
            throw new RuntimeException("Failed to parse directory", e);
        }
    }

    private List<Path> findJavaFiles(final Path rootPath) throws Exception {
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

        LOGGER.debug("Checking file: {} (fileName: {})", filePath, fileName);

        // Check file size
        try {
            if (Files.size(filePath) > parserConfig.getMaxFileSize()) {
                LOGGER.debug("Skipping large file: {}", filePath);
                return false;
            }
        } catch (Exception e) {
            LOGGER.warn("Could not check file size for: {}", filePath, e);
            return false;
        }

        // Check if it's a test file
        if (!parserConfig.isIncludeTests()
            && (fileName.contains("Test") || fileName.contains("test")
                || filePathStr.contains("/test/") || filePathStr.contains("\\test\\"))) {
            LOGGER.debug("Skipping test file: {} (fileName contains 'Test' or 'test')", filePath);
            return false;
        }

        // Check if it's a generated file
        if (fileName.contains("Generated") || fileName.contains("generated")) {
            LOGGER.debug("Skipping generated file: {}", filePath);
            return false;
        }

        // Check if it's a build file
        if (filePathStr.contains("/build/") || filePathStr.contains("\\build\\")) {
            LOGGER.debug("Skipping build file: {}", filePath);
            return false;
        }

        // Check if it's a target file
        if (filePathStr.contains("/target/") || filePathStr.contains("\\target\\")) {
            LOGGER.debug("Skipping target file: {}", filePath);
            return false;
        }

        LOGGER.debug("Including file: {}", filePath);
        return true;
    }
    
    private void parseJavaFile(final Path filePath) throws FileNotFoundException {
        LOGGER.debug("Parsing file: {}", filePath);

        ParseResult<CompilationUnit> parseResult = javaParser.parse(filePath.toFile());

        if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
            CompilationUnit cu = parseResult.getResult().get();
            JavaGraphVisitor visitor = new JavaGraphVisitor(filePath.toString());
            visitor.visit(cu, null);
        } else {
            LOGGER.warn("Failed to parse file: {}", filePath);
        }
    }
    
    private class JavaGraphVisitor extends VoidVisitorAdapter<Void> {
        
        private final String filePath;
        private String packageName = "";
        private ClassNode currentClass;
        private final List<MethodNode> currentMethods = new ArrayList<>();
        private final List<FieldNode> currentFields = new ArrayList<>();
        
        public JavaGraphVisitor(final String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void visit(final PackageDeclaration packageDecl, final Void arg) {
            this.packageName = packageDecl.getNameAsString();
            super.visit(packageDecl, arg);
        }

        @Override
        public void visit(final ClassOrInterfaceDeclaration classDecl, final Void arg) {
            // Only process public classes/interfaces
            if (classDecl.isPublic() || parserConfig.isIncludePrivate()) {
                LOGGER.info("Processing class: {}", classDecl.getNameAsString());
                currentClass = createClassNode(classDecl);
                LOGGER.info("Created class node: {}", currentClass.getId());
                graphService.saveClass(currentClass);
                
                // Clear current methods and fields for this class
                currentMethods.clear();
                currentFields.clear();
                
                // Visit methods and fields
                super.visit(classDecl, arg);
                
                // Create CONTAINS relationships for this class
                createContainsRelationships();
                
                // Create EXTENDS relationship if this class extends another
                if (classDecl.getExtendedTypes().isNonEmpty()) {
                    classDecl.getExtendedTypes().forEach(extendedType -> {
                        String parentClassName = extendedType.getNameAsString();
                        // Create a placeholder parent class node if it doesn't exist
                        ClassNode parentClass = new ClassNode(parentClassName, packageName, filePath);
                        graphService.saveClass(parentClass);
                        graphService.createRelationship(currentClass.getId(), parentClass.getId(), "EXTENDS");
                        LOGGER.debug("Created EXTENDS relationship: {} -> {}", currentClass.getName(), parentClassName);
                    });
                }

                // Create IMPLEMENTS relationships if this class implements interfaces
                if (classDecl.getImplementedTypes().isNonEmpty()) {
                    classDecl.getImplementedTypes().forEach(implementedType -> {
                        String interfaceName = implementedType.getNameAsString();
                        // Create a placeholder interface node if it doesn't exist
                        ClassNode interfaceClass = new ClassNode(interfaceName, packageName, filePath);
                        interfaceClass.setIsInterface(true);
                        graphService.saveClass(interfaceClass);
                        graphService.createRelationship(currentClass.getId(), interfaceClass.getId(), "IMPLEMENTS");
                        LOGGER.debug("Created IMPLEMENTS relationship: {} -> {}", currentClass.getName(), interfaceName);
                    });
                }
                
                currentClass = null;
            }
        }
        
        @Override
        public void visit(final MethodDeclaration methodDecl, final Void arg) {
            // Only process public methods
            if (methodDecl.isPublic() || parserConfig.isIncludePrivate()) {
                MethodNode methodNode = createMethodNode(methodDecl);
                graphService.saveMethod(methodNode);
                currentMethods.add(methodNode);

                // Detect method calls within this method
                detectMethodCalls(methodDecl, methodNode);

                // Detect field usage within this method
                detectFieldUsage(methodDecl, methodNode);
            }
        }
        
        @Override
        public void visit(FieldDeclaration fieldDecl, Void arg) {
            // Only process public fields
            if (fieldDecl.isPublic() || parserConfig.isIncludePrivate()) {
                fieldDecl.getVariables().forEach(variable -> {
                    FieldNode fieldNode = createFieldNode(variable.getNameAsString(), fieldDecl);
                    graphService.saveField(fieldNode);
                    currentFields.add(fieldNode);
                });
            }
        }
        
        private void createContainsRelationships() {
                               // Create CONTAINS relationships from class to methods
                   for (MethodNode method : currentMethods) {
                       graphService.createRelationship(currentClass.getId(), method.getId(), "CONTAINS");
                       LOGGER.debug("Created CONTAINS relationship: {} -> {}", currentClass.getName(), method.getName());
                   }

                   // Create CONTAINS relationships from class to fields
                   for (FieldNode field : currentFields) {
                       graphService.createRelationship(currentClass.getId(), field.getId(), "CONTAINS");
                       LOGGER.debug("Created CONTAINS relationship: {} -> {}", currentClass.getName(), field.getName());
                   }
        }
        
        private void detectMethodCalls(MethodDeclaration methodDecl, MethodNode methodNode) {
            // Find all method calls within this method
            methodDecl.findAll(com.github.javaparser.ast.expr.MethodCallExpr.class).forEach(call -> {
                String calledMethodName = call.getNameAsString();
                
                // Look for the called method in current methods or create a placeholder
                MethodNode calledMethod = currentMethods.stream()
                    .filter(m -> m.getName().equals(calledMethodName))
                    .findFirst()
                    .orElseGet(() -> {
                        // Create a placeholder method node
                        MethodNode placeholder = new MethodNode(calledMethodName, currentClass.getName(), packageName, filePath);
                        graphService.saveMethod(placeholder);
                        return placeholder;
                    });
                
                                       // Create CALLS relationship
                       graphService.createRelationship(methodNode.getId(), calledMethod.getId(), "CALLS");
                       LOGGER.debug("Created CALLS relationship: {} -> {}", methodNode.getName(), calledMethodName);
                   });
               }
        
        private void detectFieldUsage(MethodDeclaration methodDecl, MethodNode methodNode) {
            // Find all field access expressions within this method
            methodDecl.findAll(com.github.javaparser.ast.expr.FieldAccessExpr.class).forEach(fieldAccess -> {
                String fieldName = fieldAccess.getNameAsString();
                
                // Look for the field in current fields or create a placeholder
                FieldNode field = currentFields.stream()
                    .filter(f -> f.getName().equals(fieldName))
                    .findFirst()
                    .orElseGet(() -> {
                        // Create a placeholder field node
                        FieldNode placeholder = new FieldNode(fieldName, currentClass.getName(), packageName, filePath);
                        graphService.saveField(placeholder);
                        return placeholder;
                    });
                
                                       // Create USES relationship
                       graphService.createRelationship(methodNode.getId(), field.getId(), "USES");
                       LOGGER.debug("Created USES relationship: {} -> {}", methodNode.getName(), fieldName);
                   });
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