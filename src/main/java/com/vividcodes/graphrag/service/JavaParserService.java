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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.vividcodes.graphrag.config.ParserConfig;
import com.vividcodes.graphrag.model.dto.RepositoryMetadata;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;

/**
 * Refactored JavaParserService that orchestrates Java code parsing using focused service classes.
 * This service is responsible for file discovery, parsing coordination, and high-level workflow management.
 */
@Service
public class JavaParserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaParserService.class);

    private final ParserConfig parserConfig;
    private final RepositoryService repositoryService;
    private final ApplicationContext applicationContext;

    @Autowired
    public JavaParserService(final ParserConfig parserConfig,
                            final RepositoryService repositoryService,
                            final ApplicationContext applicationContext) {
        this.parserConfig = parserConfig;
        this.repositoryService = repositoryService;
        this.applicationContext = applicationContext;
    }

    /**
     * Parse all Java files in a directory and create graph nodes and relationships.
     * 
     * @param sourcePath The root directory path to parse
     */
    public void parseDirectory(final String sourcePath) {
        try {
            final Path rootPath = Paths.get(sourcePath);
            
            if (!Files.exists(rootPath)) {
                throw new IllegalArgumentException("Source path does not exist: " + sourcePath);
            }
            
            if (!Files.isDirectory(rootPath)) {
                throw new IllegalArgumentException("Source path is not a directory: " + sourcePath);
            }

            LOGGER.info("Starting Java parsing for directory: {}", sourcePath);
            
            // Find all Java files
            final List<Path> javaFiles = findJavaFiles(rootPath);
            LOGGER.info("Found {} Java files to parse in directory", javaFiles.size());
            
            // Parse each Java file
            for (final Path javaFile : javaFiles) {
                try {
                    parseJavaFile(javaFile);
                } catch (final Exception e) {
                    LOGGER.error("Error parsing file: {}", javaFile, e);
                }
            }
            
            LOGGER.info("Completed parsing {} Java files", javaFiles.size());
            
        } catch (final Exception e) {
            LOGGER.error("Error parsing directory: {}", sourcePath, e);
            throw new RuntimeException("Failed to parse directory: " + sourcePath, e);
        }
    }

    /**
     * Find all Java files in a directory recursively.
     * 
     * @param rootPath The root directory to search
     * @return List of Java file paths
     * @throws Exception if there's an error accessing the file system
     */
    private List<Path> findJavaFiles(final Path rootPath) throws Exception {
        LOGGER.info("Finding Java files in directory: {}", rootPath);
        
        try (final Stream<Path> paths = Files.walk(rootPath)) {
            final List<Path> javaFiles = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(this::shouldIncludeFile)
                .collect(Collectors.toList());
            
            LOGGER.info("Found {} Java files in directory {}", javaFiles.size(), rootPath);
            
            // Log each file for debugging
            javaFiles.forEach(file -> LOGGER.debug("Found Java file: {}", file));
            
            return javaFiles;
        }
    }

    /**
     * Determine if a file should be included in parsing.
     * 
     * @param filePath The file path to check
     * @return true if the file should be included, false otherwise
     */
    private boolean shouldIncludeFile(final Path filePath) {
        final String fileName = filePath.getFileName().toString();
        
        LOGGER.debug("Checking file: {} (fileName: {})", filePath, fileName);
        
        // Skip test files unless explicitly included
        if (!parserConfig.isIncludeTests() && (fileName.contains("Test") || fileName.contains("test"))) {
            LOGGER.debug("Skipping test file: {} (fileName contains 'Test' or 'test')", filePath);
            return false;
        }
        
        // Check file size
        try {
            final long fileSize = Files.size(filePath);
            if (fileSize > parserConfig.getMaxFileSize()) {
                LOGGER.warn("Skipping large file: {} (size: {} bytes, max: {} bytes)", 
                          filePath, fileSize, parserConfig.getMaxFileSize());
                return false;
            }
        } catch (final Exception e) {
            LOGGER.warn("Could not check file size for: {}", filePath, e);
            return false;
        }
        
        LOGGER.debug("Including file: {}", filePath);
        return true;
    }

    /**
     * Parse a single Java file and create graph nodes and relationships.
     * 
     * @param filePath The path to the Java file
     * @throws FileNotFoundException if the file cannot be found
     */
    private void parseJavaFile(final Path filePath) throws FileNotFoundException {
        LOGGER.info("Parsing file: {}", filePath);
        
        // Get repository metadata for this file
        final RepositoryMetadata repoMetadata = repositoryService.detectRepositoryMetadata(filePath);
        LOGGER.info("Repository metadata for {}: {}", filePath, 
                   repoMetadata != null ? repoMetadata.getRepositoryName() : "null");
        
        // Create or update repository node
        final RepositoryNode repository = repositoryService.createOrUpdateRepository(repoMetadata);
        LOGGER.info("Repository node for {}: {}", filePath, 
                   repository != null ? repository.getName() : "null");
        
        // Detect and create sub-projects for this repository
        List<SubProjectNode> subProjects = new ArrayList<>();
        if (repository != null) {
            subProjects = repositoryService.detectAndCreateSubProjects(repository);
            LOGGER.debug("Found {} sub-projects in repository {}", subProjects.size(), repository.getName());
        } else {
            LOGGER.debug("No repository found, skipping sub-project detection");
        }
        
        // Find the containing sub-project for this file
        final SubProjectNode containingSubProject = findContainingSubProject(filePath, subProjects, repository);
        LOGGER.debug("Java file {} belongs to sub-project: {}", filePath, 
                    containingSubProject != null ? containingSubProject.getName() : "none");
        
        // Parse the Java file
        final JavaParser javaParser = new JavaParser();
        final ParseResult<CompilationUnit> parseResult;
        try {
            parseResult = javaParser.parse(filePath);
        } catch (final java.io.IOException e) {
            LOGGER.error("IOException while parsing file: {}", filePath, e);
            throw new FileNotFoundException("Could not read file: " + filePath);
        }
        
        LOGGER.info("Parse result for {}: {}", filePath, parseResult.isSuccessful() ? "successful" : "failed");
        
        if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
            final CompilationUnit cu = parseResult.getResult().get();
            LOGGER.info("Successfully parsed file: {}. CompilationUnit has {} types", filePath, cu.getTypes().size());
            
            // Create a new visitor instance for this parsing operation
            final JavaGraphVisitor visitor = applicationContext.getBean(JavaGraphVisitor.class);
            visitor.initialize(filePath.toString(), repoMetadata, containingSubProject);
            
            LOGGER.info("Created JavaGraphVisitor for file: {} with sub-project: {}", filePath, 
                       containingSubProject != null ? containingSubProject.getName() : "none");
            
            // Visit the compilation unit
            cu.accept(visitor, null);
            
            LOGGER.info("Visitor visit completed successfully for file: {}", filePath);
            LOGGER.info("Visitor created {} nodes for file: {}", visitor.getCreatedNodes().size(), filePath);
            
            // Log repository relationship creation
            if (repository == null && !visitor.getCreatedNodes().isEmpty()) {
                LOGGER.warn("No repository found, but created {} nodes for file: {}", 
                          visitor.getCreatedNodes().size(), filePath);
            }
        } else {
            LOGGER.error("Failed to parse file: {}", filePath);
            parseResult.getProblems().forEach(problem -> 
                LOGGER.error("Parse problem: {}", problem.getMessage()));
        }
    }

    /**
     * Find the sub-project that contains a given Java file.
     * 
     * @param javaFilePath The path to the Java file
     * @param subProjects List of sub-projects to search
     * @param repository The repository containing the sub-projects
     * @return The containing sub-project, or null if none found
     */
    private SubProjectNode findContainingSubProject(final Path javaFilePath, 
                                                   final List<SubProjectNode> subProjects, 
                                                   final RepositoryNode repository) {
        if (subProjects == null || subProjects.isEmpty()) {
            LOGGER.debug("No sub-projects found, file {} not contained in any sub-project", javaFilePath);
            return null;
        }
        
        if (repository == null || repository.getLocalPath() == null) {
            LOGGER.debug("No repository or local path, cannot determine sub-project for file {}", javaFilePath);
            return null;
        }

        final Path absoluteJavaPath = javaFilePath.toAbsolutePath().normalize();
        final Path repositoryPath = Paths.get(repository.getLocalPath()).toAbsolutePath().normalize();
        SubProjectNode bestMatch = null;
        String longestMatchingPath = "";

        for (final SubProjectNode subProject : subProjects) {
            try {
                Path subProjectPath;
                final String subProjectRelativePath = subProject.getPath();

                if (subProjectRelativePath == null || subProjectRelativePath.trim().isEmpty()) {
                    subProjectPath = repositoryPath;
                } else {
                    subProjectPath = repositoryPath.resolve(subProjectRelativePath).normalize();
                }

                if (absoluteJavaPath.startsWith(subProjectPath)) {
                    final String subProjectPathStr = subProjectPath.toString();
                    if (subProjectPathStr.length() > longestMatchingPath.length()) {
                        bestMatch = subProject;
                        longestMatchingPath = subProjectPathStr;
                    }
                }
            } catch (final Exception e) {
                LOGGER.warn("Error checking sub-project path for {}: {}", subProject.getName(), e.getMessage());
            }
        }

        if (bestMatch != null) {
            LOGGER.debug("File {} belongs to sub-project: {} (path: {})", 
                        javaFilePath, bestMatch.getName(), longestMatchingPath);
        } else {
            LOGGER.debug("File {} does not belong to any sub-project", javaFilePath);
        }

        return bestMatch;
    }
}
