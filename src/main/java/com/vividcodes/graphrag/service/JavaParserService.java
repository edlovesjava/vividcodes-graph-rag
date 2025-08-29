package com.vividcodes.graphrag.service;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.vividcodes.graphrag.config.ParserConfig;
import com.vividcodes.graphrag.model.dto.RepositoryMetadata;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;

@Service
public class JavaParserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaParserService.class);

    private final ParserConfig parserConfig;
    private final GraphService graphService;
    private final RepositoryService repositoryService;
    private final JavaParser javaParser;

    @Autowired
    public JavaParserService(final ParserConfig parserConfig, final GraphService graphService, final RepositoryService repositoryService) {
        this.parserConfig = parserConfig;
        this.graphService = graphService;
        this.repositoryService = repositoryService;
        this.javaParser = new JavaParser();
    }

    public void parseDirectory(final String sourcePath) {
        try {
            Path rootPath = Paths.get(sourcePath);
            if (!Files.exists(rootPath)) {
                throw new IllegalArgumentException("Source path does not exist: " + sourcePath);
            }

            List<Path> javaFiles;
            if (Files.isRegularFile(rootPath)) {
                // If it's a single file, check if it's a Java file
                if (rootPath.toString().endsWith(".java") && shouldIncludeFile(rootPath)) {
                    javaFiles = List.of(rootPath);
                    LOGGER.info("Processing single Java file: {}", rootPath);
                } else {
                    LOGGER.warn("File {} is not a Java file or should be excluded", rootPath);
                    return;
                }
            } else {
                // If it's a directory, find all Java files
                javaFiles = findJavaFiles(rootPath);
                LOGGER.info("Found {} Java files to parse in directory", javaFiles.size());
            }

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
        LOGGER.info("Finding Java files in directory: {}", rootPath);
        try (Stream<Path> paths = Files.walk(rootPath)) {
            List<Path> javaFiles = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(this::shouldIncludeFile)
                .collect(Collectors.toList());
            
            LOGGER.info("Found {} Java files in directory {}", javaFiles.size(), rootPath);
            for (Path file : javaFiles) {
                LOGGER.debug("Found Java file: {}", file);
            }
            
            return javaFiles;
        }
    }
    
    boolean shouldIncludeFile(Path filePath) {
        if (filePath == null || filePath.getFileName() == null) {
            LOGGER.debug("Skipping null or invalid file path: {}", filePath);
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
        LOGGER.info("Parsing file: {}", filePath);

        // Detect repository metadata for this file
        RepositoryMetadata repoMetadata = repositoryService.detectRepositoryMetadata(filePath);
        LOGGER.info("Repository metadata for {}: {}", filePath, repoMetadata != null ? "found" : "null");
        
        RepositoryNode repository = repositoryService.createOrUpdateRepository(repoMetadata);
        LOGGER.info("Repository node for {}: {}", filePath, repository != null ? "created/found" : "null");

        // Detect and create sub-projects for this repository
        List<SubProjectNode> subProjects = List.of(); // Default to empty list
        if (repository != null) {
            subProjects = repositoryService.detectAndCreateSubProjects(repository);
            LOGGER.debug("Found {} sub-projects in repository {}", subProjects.size(), repository.getName());
        } else {
            LOGGER.debug("No repository found, skipping sub-project detection");
        }
        
        // Determine which sub-project this Java file belongs to
        SubProjectNode containingSubProject = findContainingSubProject(filePath, subProjects, repository);
        LOGGER.debug("Java file {} belongs to sub-project: {}", 
                    filePath, containingSubProject != null ? containingSubProject.getName() : "none");

        ParseResult<CompilationUnit> parseResult = javaParser.parse(filePath.toFile());
        LOGGER.info("Parse result for {}: {}", filePath, parseResult.isSuccessful() ? "successful" : "failed");
        
        if (!parseResult.isSuccessful()) {
            LOGGER.error("Parsing failed for file: {}. Problems: {}", filePath, parseResult.getProblems());
        }

        if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
            CompilationUnit cu = parseResult.getResult().get();
            LOGGER.info("Successfully parsed file: {}. CompilationUnit has {} types", filePath, cu.getTypes().size());
            JavaGraphVisitor visitor = new JavaGraphVisitor(filePath.toString(), repoMetadata, containingSubProject);
            LOGGER.info("Created JavaGraphVisitor for file: {} with sub-project: {}", 
                       filePath, containingSubProject != null ? containingSubProject.getName() : "none");
            try {
                visitor.visit(cu, null);
                LOGGER.info("Visitor visit completed successfully for file: {}", filePath);
            } catch (Exception e) {
                LOGGER.error("Exception during visitor visit for file: {}", filePath, e);
            }
            
            LOGGER.info("Visitor created {} nodes for file: {}", visitor.createdNodes.size(), filePath);
            
            // Link all created nodes to the repository (even if repository is null, still create the nodes)
            if (!visitor.createdNodes.isEmpty()) {
                if (repository != null) {
                    repositoryService.linkNodesToRepository(visitor.createdNodes, repository);
                    LOGGER.info("Linked {} nodes to repository: {}", visitor.createdNodes.size(), repository.getName());
                } else {
                    LOGGER.warn("No repository found, but created {} nodes for file: {}", visitor.createdNodes.size(), filePath);
                }
            } else {
                LOGGER.warn("No nodes created for file: {}", filePath);
            }
        } else {
            LOGGER.warn("Failed to parse file: {}", filePath);
        }
    }
    
    private class JavaGraphVisitor extends VoidVisitorAdapter<Void> {
        
        private final String filePath;
        private final RepositoryMetadata repositoryMetadata;
        private final SubProjectNode containingSubProject;
        private String packageName = "";
        private ClassNode currentClass;
        private final List<MethodNode> currentMethods = new ArrayList<>();
        private final List<FieldNode> currentFields = new ArrayList<>();
        private final List<Object> createdNodes = new ArrayList<>();
        private PackageNode currentPackage;
        
        // Track imported classes for dependency analysis
        private final Map<String, String> importedClasses = new HashMap<>(); // className -> fullyQualifiedName
        
        public JavaGraphVisitor(final String filePath, final RepositoryMetadata repositoryMetadata, final SubProjectNode containingSubProject) {
            this.filePath = filePath;
            this.repositoryMetadata = repositoryMetadata;
            this.containingSubProject = containingSubProject;
        }

        @Override
        public void visit(final PackageDeclaration packageDecl, final Void arg) {
            LOGGER.info("Visiting package declaration: {}", packageDecl.getNameAsString());
            this.packageName = packageDecl.getNameAsString();
            
            // Create package node
            PackageNode packageNode = new PackageNode(packageName, packageName, filePath);
            LOGGER.info("Created package node: {} with ID: {}", packageNode.getName(), packageNode.getId());
            graphService.savePackage(packageNode);
            createdNodes.add(packageNode);
            this.currentPackage = packageNode;  // Set the current package for relationship creation
            LOGGER.info("Added package node to createdNodes list. Total nodes: {}", createdNodes.size());
            
            super.visit(packageDecl, arg);
        }

        @Override
        public void visit(final ImportDeclaration importDecl, final Void arg) {
            String fullyQualifiedName = importDecl.getNameAsString();
            LOGGER.debug("Processing import: {}", fullyQualifiedName);
            
            // Extract the simple class name from the fully qualified name
            String className = extractSimpleClassName(fullyQualifiedName);
            
            // Store the mapping for later class name resolution
            importedClasses.put(className, fullyQualifiedName);
            
            // Create or get the imported class node (creates it in the database)
            getOrCreateClassNode(className, fullyQualifiedName);
            
            // We'll create the USES relationship when we process the class that contains this import
            // For now, just track the import for later processing
            LOGGER.debug("Registered import: {} -> {}", className, fullyQualifiedName);
            
            super.visit(importDecl, arg);
        }

        @Override
        public void visit(final ClassOrInterfaceDeclaration classDecl, final Void arg) {
                    // TEMPORARILY: Process all classes/interfaces for debugging
        if (true || classDecl.isPublic() || parserConfig.isIncludePrivate()) {
                LOGGER.info("Processing class: {}", classDecl.getNameAsString());
                currentClass = createClassNode(classDecl);
                LOGGER.info("Created class node: {}", currentClass.getId());
                graphService.saveClass(currentClass);
                createdNodes.add(currentClass);
                
                // Clear current methods and fields for this class
                currentMethods.clear();
                currentFields.clear();
                
                // Visit methods and fields
                super.visit(classDecl, arg);
                
                // Create CONTAINS relationships for this class
                createContainsRelationships();
                
                // Create USES relationships for imported classes
                createImportUsesRelationships();
                
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
                    // TEMPORARILY: Process all methods for debugging
        if (true || methodDecl.isPublic() || parserConfig.isIncludePrivate()) {
                MethodNode methodNode = createMethodNode(methodDecl);
                graphService.saveMethod(methodNode);
                currentMethods.add(methodNode);
                createdNodes.add(methodNode);

                // Detect method calls within this method
                detectMethodCalls(methodDecl, methodNode);

                // Detect field usage within this method
                detectFieldUsage(methodDecl, methodNode);
            }
        }
        
        @Override
        public void visit(FieldDeclaration fieldDecl, Void arg) {
                    // TEMPORARILY: Process all fields for debugging
        if (true || fieldDecl.isPublic() || parserConfig.isIncludePrivate()) {
                fieldDecl.getVariables().forEach(variable -> {
                    String fieldName = variable.getNameAsString();
                    FieldNode fieldNode = createFieldNode(fieldName, fieldDecl);
                    graphService.saveField(fieldNode);
                    currentFields.add(fieldNode);
                    createdNodes.add(fieldNode);
                    
                    // Detect object instantiations in field initializations
                    variable.getInitializer().ifPresent(initializer -> {
                        initializer.findAll(ObjectCreationExpr.class).forEach(newExpr -> {
                            String className = newExpr.getType().getNameAsString();
                            LOGGER.debug("Detected object instantiation in field {}: new {}", fieldName, className);
                            
                            // Resolve the class name using imports
                            String fullyQualifiedName = importedClasses.getOrDefault(className, className);
                            
                            // Create or get the instantiated class node
                            ClassNode instantiatedClass = getOrCreateClassNode(className, fullyQualifiedName);
                            
                            // Create USES relationship with instantiation metadata
                            Map<String, Object> relationshipMetadata = Map.of(
                                "type", "instantiation",
                                "fullyQualifiedName", fullyQualifiedName,
                                "context", "field:" + fieldName,
                                "isExternal", instantiatedClass.getIsExternal()
                            );
                            
                            graphService.createRelationship(
                                currentClass.getId(), 
                                instantiatedClass.getId(), 
                                "USES", 
                                relationshipMetadata
                            );
                            
                            LOGGER.debug("Created USES relationship for field instantiation: {} -> {} ({})", 
                                       currentClass.getName(), className, fullyQualifiedName);
                        });
                    });
                });
            }
        }
        
        private void createContainsRelationships() {
            // Create CONTAINS relationships from package to class
            if (currentPackage != null && currentClass != null) {
                graphService.createRelationship(currentPackage.getId(), currentClass.getId(), "CONTAINS");
                LOGGER.debug("Created CONTAINS relationship: package {} -> class {}", currentPackage.getName(), currentClass.getName());
            }

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
            
            // Create CONTAINS relationships from SubProject to CodeElements (if SubProject exists)
            if (containingSubProject != null) {
                // SubProject -> Package
                if (currentPackage != null) {
                    graphService.createRelationship(containingSubProject.getId(), currentPackage.getId(), "CONTAINS");
                    LOGGER.debug("Created CONTAINS relationship: sub-project {} -> package {}", 
                               containingSubProject.getName(), currentPackage.getName());
                }
                
                // SubProject -> Class (as backup if no package)
                if (currentClass != null) {
                    graphService.createRelationship(containingSubProject.getId(), currentClass.getId(), "CONTAINS");
                    LOGGER.debug("Created CONTAINS relationship: sub-project {} -> class {}", 
                               containingSubProject.getName(), currentClass.getName());
                }
            }
        }
        
        private void detectMethodCalls(MethodDeclaration methodDecl, MethodNode methodNode) {
            // Find all method calls within this method
            methodDecl.findAll(MethodCallExpr.class).forEach(call -> {
                String calledMethodName = call.getNameAsString();
                
                // Check if this is a static method call (has a scope like Class.method())
                if (call.getScope().isPresent()) {
                    String scopeExpr = call.getScope().get().toString();
                    LOGGER.debug("Detected method call with scope: {}.{}", scopeExpr, calledMethodName);
                    
                    // Check if scope refers to a known imported class
                    if (importedClasses.containsKey(scopeExpr)) {
                        String targetClassFQN = importedClasses.get(scopeExpr);
                        LOGGER.debug("Static method call detected: {}.{} -> {}", scopeExpr, calledMethodName, targetClassFQN);
                        
                        // Create or get the target class node
                        ClassNode targetClass = getOrCreateClassNode(scopeExpr, targetClassFQN);
                        
                        // Create USES relationship from current class to target class (static method usage)
                        if (currentClass != null) {
                            graphService.createRelationship(
                                currentClass.getId(), 
                                targetClass.getId(), 
                                "USES",
                                Map.of(
                                    "type", "static_method_call",
                                    "method_name", calledMethodName,
                                    "context", "method:" + methodNode.getName()
                                )
                            );
                            LOGGER.debug("Created USES relationship: {} -> {} (static method call: {})", 
                                currentClass.getName(), targetClass.getName(), calledMethodName);
                        }
                    } else {
                        // Could be a field reference or other class - try to resolve
                        LOGGER.debug("Method call scope not in imports: {}.{}", scopeExpr, calledMethodName);
                    }
                } else {
                    // Instance method call within the same class
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
                }
            });
            
            // Find all object instantiations (new expressions) within this method
            methodDecl.findAll(ObjectCreationExpr.class).forEach(newExpr -> {
                String className = newExpr.getType().getNameAsString();
                LOGGER.debug("Detected object instantiation: new {} in method {}", className, methodNode.getName());
                
                // Resolve the class name using imports
                String fullyQualifiedName = importedClasses.getOrDefault(className, className);
                
                // Create or get the instantiated class node
                ClassNode instantiatedClass = getOrCreateClassNode(className, fullyQualifiedName);
                
                // Create USES relationship with instantiation metadata
                Map<String, Object> relationshipMetadata = Map.of(
                    "type", "instantiation",
                    "fullyQualifiedName", fullyQualifiedName,
                    "context", "method:" + methodNode.getName(),
                    "isExternal", instantiatedClass.getIsExternal()
                );
                
                graphService.createRelationship(
                    currentClass.getId(), 
                    instantiatedClass.getId(), 
                    "USES", 
                    relationshipMetadata
                );
                
                LOGGER.debug("Created USES relationship for instantiation: {} -> {} ({})", 
                           currentClass.getName(), className, fullyQualifiedName);
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
            
            // Fully qualified name is now set in constructor
            
            classNode.setVisibility(getVisibility(classDecl));
            classNode.setModifiers(getModifiers(classDecl));
            classNode.setIsInterface(classDecl.isInterface());
            classNode.setIsEnum(classDecl.isEnumDeclaration());
            classNode.setIsAnnotation(classDecl.isAnnotationDeclaration());
            
            // Set repository metadata if available
            if (repositoryMetadata != null) {
                classNode.setRepositoryId(repositoryMetadata.getRepositoryId());
                classNode.setRepositoryName(repositoryMetadata.getRepositoryName());
                classNode.setRepositoryUrl(repositoryMetadata.getRepositoryUrl());
                classNode.setBranch(repositoryMetadata.getBranch());
                classNode.setCommitHash(repositoryMetadata.getCommitHash());
                classNode.setCommitDate(repositoryMetadata.getCommitDate());
                classNode.setFileRelativePath(repositoryMetadata.getFileRelativePath());
            }
            
            // Set sub-project context if available
            if (containingSubProject != null) {
                // For now, we'll create the relationships instead of storing IDs
                // TODO: Add subProjectId property to ClassNode in future enhancement
                LOGGER.debug("Class {} will be linked to sub-project {}", 
                           classNode.getName(), containingSubProject.getName());
            }
            
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
        
        /**
         * Extract simple class name from fully qualified name
         */
        private String extractSimpleClassName(String fullyQualifiedName) {
            if (fullyQualifiedName == null || fullyQualifiedName.isEmpty()) {
                return "";
            }
            int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
            return lastDotIndex >= 0 ? fullyQualifiedName.substring(lastDotIndex + 1) : fullyQualifiedName;
        }
        
        /**
         * Create or get a class node for dependency tracking
         */
        private ClassNode getOrCreateClassNode(String simpleClassName, String fullyQualifiedName) {
            // Determine package name from fully qualified name
            String packageName = "";
            int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                packageName = fullyQualifiedName.substring(0, lastDotIndex);
            }
            
            // Create class node (will be external/placeholder if not from our codebase)
            ClassNode classNode = new ClassNode(simpleClassName, packageName, "external");
            classNode.setFullyQualifiedName(fullyQualifiedName);
            
            // Mark as external dependency (we'll refine this logic later)
            classNode.setIsExternal(isExternalClass(fullyQualifiedName));
            
            // Save the class node
            graphService.saveClass(classNode);
            LOGGER.debug("Created/retrieved class node for: {}", fullyQualifiedName);
            
            return classNode;
        }
        
        /**
         * Determine if a class is external (from framework/libraries)
         */
        private boolean isExternalClass(String fullyQualifiedName) {
            // Basic heuristic: consider classes external if they're from common frameworks/libraries
            return fullyQualifiedName.startsWith("java.") ||
                   fullyQualifiedName.startsWith("javax.") ||
                   fullyQualifiedName.startsWith("org.springframework.") ||
                   fullyQualifiedName.startsWith("org.junit.") ||
                   fullyQualifiedName.startsWith("org.slf4j.");
        }
        
        /**
         * Create USES relationships for all imported classes from the current class
         */
        private void createImportUsesRelationships() {
            if (currentClass == null || importedClasses.isEmpty()) {
                return;
            }
            
            for (Map.Entry<String, String> entry : importedClasses.entrySet()) {
                String className = entry.getKey();
                String fullyQualifiedName = entry.getValue();
                
                // Get or create the imported class node
                ClassNode importedClass = getOrCreateClassNode(className, fullyQualifiedName);
                
                // Create USES relationship with metadata
                Map<String, Object> relationshipMetadata = Map.of(
                    "type", "import",
                    "fullyQualifiedName", fullyQualifiedName,
                    "context", "class_level",
                    "isExternal", importedClass.getIsExternal()
                );
                
                graphService.createRelationship(
                    currentClass.getId(), 
                    importedClass.getId(), 
                    "USES", 
                    relationshipMetadata
                );
                
                LOGGER.debug("Created USES relationship for import: {} -> {} ({})", 
                           currentClass.getName(), className, fullyQualifiedName);
            }
        }
    }
    
    /**
     * Determine which sub-project contains the given Java file
     * Returns the most specific (longest path) sub-project that contains the file
     */
    private SubProjectNode findContainingSubProject(Path javaFilePath, List<SubProjectNode> subProjects, RepositoryNode repository) {
        if (subProjects == null || subProjects.isEmpty()) {
            LOGGER.debug("No sub-projects found, file {} not contained in any sub-project", javaFilePath);
            return null;
        }
        
        if (repository == null || repository.getLocalPath() == null) {
            LOGGER.debug("No repository or local path available, cannot determine sub-project for file {}", javaFilePath);
            return null;
        }
        
        Path absoluteJavaPath = javaFilePath.toAbsolutePath().normalize();
        Path repositoryPath = Paths.get(repository.getLocalPath()).toAbsolutePath().normalize();
        SubProjectNode bestMatch = null;
        String longestMatchingPath = "";
        
        // Find the sub-project with the longest path that still contains the Java file
        for (SubProjectNode subProject : subProjects) {
            try {
                // Construct absolute sub-project path by combining repository path + relative sub-project path
                Path subProjectPath;
                String subProjectRelativePath = subProject.getPath();
                
                if (subProjectRelativePath == null || subProjectRelativePath.trim().isEmpty()) {
                    // Root sub-project: use repository path directly
                    subProjectPath = repositoryPath;
                } else {
                    // Combine repository path with sub-project relative path
                    subProjectPath = repositoryPath.resolve(subProjectRelativePath).normalize();
                }
                
                // Check if the Java file is within this sub-project's directory
                if (absoluteJavaPath.startsWith(subProjectPath)) {
                    String subProjectPathStr = subProjectPath.toString();
                    // If this path is longer (more specific), it's a better match
                    if (subProjectPathStr.length() > longestMatchingPath.length()) {
                        bestMatch = subProject;
                        longestMatchingPath = subProjectPathStr;
                        LOGGER.debug("File {} found better match in sub-project {} at resolved path {}", 
                                   javaFilePath, subProject.getName(), subProjectPath);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Error checking if file {} is in sub-project {}: {}", 
                           javaFilePath, subProject.getName(), e.getMessage());
            }
        }
        
        if (bestMatch != null) {
            LOGGER.debug("File {} is contained in sub-project {} (most specific match)", 
                       javaFilePath, bestMatch.getName());
        } else {
            LOGGER.debug("File {} is not contained in any sub-project", javaFilePath);
        }
        
        return bestMatch;
    }
} 