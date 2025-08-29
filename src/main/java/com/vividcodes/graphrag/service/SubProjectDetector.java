package com.vividcodes.graphrag.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vividcodes.graphrag.model.dto.SubProjectMetadata;

/**
 * Service for detecting sub-projects within a repository based on common project indicators
 * like build files (pom.xml, build.gradle, package.json, etc.)
 */
@Service
public class SubProjectDetector {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SubProjectDetector.class);
    
    // Common build file patterns
    private static final String MAVEN_BUILD_FILE = "pom.xml";
    private static final String GRADLE_BUILD_FILE = "build.gradle";
    private static final String GRADLE_KOTLIN_BUILD_FILE = "build.gradle.kts";
    private static final String NPM_BUILD_FILE = "package.json";
    private static final String GRADLE_SETTINGS_FILE = "settings.gradle";
    private static final String GRADLE_KOTLIN_SETTINGS_FILE = "settings.gradle.kts";
    
    // Common source directory patterns
    private static final List<String> JAVA_SOURCE_PATTERNS = Arrays.asList(
        "src/main/java", "src/main/kotlin", "src/main/groovy"
    );
    private static final List<String> JAVA_TEST_PATTERNS = Arrays.asList(
        "src/test/java", "src/test/kotlin", "src/test/groovy"
    );
    
    /**
     * Detect all sub-projects within a repository root directory
     * 
     * @param repositoryPath The root path of the repository
     * @param repositoryId The ID of the parent repository
     * @return List of detected sub-project metadata
     */
    public List<SubProjectMetadata> detectSubProjects(final String repositoryPath, final String repositoryId) {
        LOGGER.info("Starting sub-project detection in repository: {}", repositoryPath);
        
        Path rootPath = Paths.get(repositoryPath);
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            LOGGER.warn("Repository path does not exist or is not a directory: {}", repositoryPath);
            return new ArrayList<>();
        }
        
        List<SubProjectMetadata> subProjects = new ArrayList<>();
        LocalDateTime detectionTime = LocalDateTime.now();
        
        try {
            // Detect Maven projects
            subProjects.addAll(detectMavenProjects(rootPath, repositoryId, detectionTime));
            
            // Detect Gradle projects
            subProjects.addAll(detectGradleProjects(rootPath, repositoryId, detectionTime));
            
            // Detect NPM projects (for completeness)
            subProjects.addAll(detectNpmProjects(rootPath, repositoryId, detectionTime));
            
            LOGGER.info("Detected {} sub-projects in repository: {}", subProjects.size(), repositoryPath);
            
        } catch (IOException e) {
            LOGGER.error("Error during sub-project detection in repository: {}", repositoryPath, e);
        }
        
        return subProjects;
    }
    
    /**
     * Detect Maven projects by looking for pom.xml files
     */
    private List<SubProjectMetadata> detectMavenProjects(final Path rootPath, final String repositoryId, 
                                                         final LocalDateTime detectionTime) throws IOException {
        LOGGER.debug("Detecting Maven projects in: {}", rootPath);
        
        List<SubProjectMetadata> mavenProjects = new ArrayList<>();
        
        // Find all pom.xml files in the repository
        try (var paths = Files.walk(rootPath)) {
            List<Path> pomFiles = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equals(MAVEN_BUILD_FILE))
                .collect(Collectors.toList());
            
            for (Path pomFile : pomFiles) {
                try {
                    SubProjectMetadata subProject = createMavenSubProject(pomFile, rootPath, repositoryId, detectionTime);
                    if (subProject != null) {
                        mavenProjects.add(subProject);
                        LOGGER.debug("Detected Maven project: {} at {}", subProject.getName(), subProject.getPath());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to process Maven project at {}: {}", pomFile, e.getMessage());
                }
            }
        }
        
        LOGGER.info("Detected {} Maven projects", mavenProjects.size());
        return mavenProjects;
    }
    
    /**
     * Detect Gradle projects by looking for build.gradle files
     */
    private List<SubProjectMetadata> detectGradleProjects(final Path rootPath, final String repositoryId,
                                                          final LocalDateTime detectionTime) throws IOException {
        LOGGER.debug("Detecting Gradle projects in: {}", rootPath);
        
        List<SubProjectMetadata> gradleProjects = new ArrayList<>();
        
        // Find all build.gradle files in the repository
        try (var paths = Files.walk(rootPath)) {
            List<Path> buildFiles = paths
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String fileName = path.getFileName().toString();
                    return fileName.equals(GRADLE_BUILD_FILE) || fileName.equals(GRADLE_KOTLIN_BUILD_FILE);
                })
                .collect(Collectors.toList());
            
            for (Path buildFile : buildFiles) {
                try {
                    SubProjectMetadata subProject = createGradleSubProject(buildFile, rootPath, repositoryId, detectionTime);
                    if (subProject != null) {
                        gradleProjects.add(subProject);
                        LOGGER.debug("Detected Gradle project: {} at {}", subProject.getName(), subProject.getPath());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to process Gradle project at {}: {}", buildFile, e.getMessage());
                }
            }
        }
        
        LOGGER.info("Detected {} Gradle projects", gradleProjects.size());
        return gradleProjects;
    }
    
    /**
     * Detect NPM projects by looking for package.json files
     */
    private List<SubProjectMetadata> detectNpmProjects(final Path rootPath, final String repositoryId,
                                                       final LocalDateTime detectionTime) throws IOException {
        LOGGER.debug("Detecting NPM projects in: {}", rootPath);
        
        List<SubProjectMetadata> npmProjects = new ArrayList<>();
        
        // Find all package.json files in the repository
        try (var paths = Files.walk(rootPath)) {
            List<Path> packageFiles = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equals(NPM_BUILD_FILE))
                .filter(path -> !path.toString().contains("node_modules")) // Exclude dependencies
                .collect(Collectors.toList());
            
            for (Path packageFile : packageFiles) {
                try {
                    SubProjectMetadata subProject = createNpmSubProject(packageFile, rootPath, repositoryId, detectionTime);
                    if (subProject != null) {
                        npmProjects.add(subProject);
                        LOGGER.debug("Detected NPM project: {} at {}", subProject.getName(), subProject.getPath());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to process NPM project at {}: {}", packageFile, e.getMessage());
                }
            }
        }
        
        LOGGER.info("Detected {} NPM projects", npmProjects.size());
        return npmProjects;
    }
    
    /**
     * Create SubProjectMetadata for a Maven project
     */
    private SubProjectMetadata createMavenSubProject(final Path pomFile, final Path rootPath, 
                                                    final String repositoryId, final LocalDateTime detectionTime) {
        Path projectDir = pomFile.getParent();
        String relativePath = rootPath.relativize(projectDir).toString();
        
        // Generate project name from directory name or use "root" for root pom
        String projectName = relativePath.isEmpty() ? "root" : projectDir.getFileName().toString();
        
        // Create unique ID
        String projectId = generateSubProjectId(repositoryId, relativePath, "maven");
        
        SubProjectMetadata subProject = new SubProjectMetadata();
        subProject.setName(projectName);
        subProject.setPath(relativePath);
        subProject.setType("maven");
        // Set ID AFTER setting path and type to avoid auto-regeneration overriding it
        subProject.setId(projectId);
        subProject.setBuildFile(MAVEN_BUILD_FILE);
        subProject.setRepositoryId(repositoryId);
        subProject.setDetectedAt(detectionTime);
        
        // Detect source directories
        subProject.setSourceDirectories(detectSourceDirectories(projectDir, JAVA_SOURCE_PATTERNS));
        subProject.setTestDirectories(detectSourceDirectories(projectDir, JAVA_TEST_PATTERNS));
        
        // TODO: Parse pom.xml to extract more metadata (dependencies, version, description)
        // For now, set basic info
        subProject.setDescription("Maven project detected at " + relativePath);
        
        return subProject;
    }
    
    /**
     * Create SubProjectMetadata for a Gradle project
     */
    private SubProjectMetadata createGradleSubProject(final Path buildFile, final Path rootPath,
                                                     final String repositoryId, final LocalDateTime detectionTime) {
        Path projectDir = buildFile.getParent();
        String relativePath = rootPath.relativize(projectDir).toString();
        
        // Generate project name from directory name or use "root" for root build file
        String projectName = relativePath.isEmpty() ? "root" : projectDir.getFileName().toString();
        
        // Create unique ID
        String projectId = generateSubProjectId(repositoryId, relativePath, "gradle");
        
        SubProjectMetadata subProject = new SubProjectMetadata();
        subProject.setName(projectName);
        subProject.setPath(relativePath);
        subProject.setType("gradle");
        // Set ID AFTER setting path and type to avoid auto-regeneration overriding it
        subProject.setId(projectId);
        subProject.setBuildFile(buildFile.getFileName().toString());
        subProject.setRepositoryId(repositoryId);
        subProject.setDetectedAt(detectionTime);
        
        // Detect source directories
        subProject.setSourceDirectories(detectSourceDirectories(projectDir, JAVA_SOURCE_PATTERNS));
        subProject.setTestDirectories(detectSourceDirectories(projectDir, JAVA_TEST_PATTERNS));
        
        // TODO: Parse build.gradle to extract more metadata (dependencies, version, description)
        // For now, set basic info
        subProject.setDescription("Gradle project detected at " + relativePath);
        
        return subProject;
    }
    
    /**
     * Create SubProjectMetadata for an NPM project
     */
    private SubProjectMetadata createNpmSubProject(final Path packageFile, final Path rootPath,
                                                  final String repositoryId, final LocalDateTime detectionTime) {
        Path projectDir = packageFile.getParent();
        String relativePath = rootPath.relativize(projectDir).toString();
        
        // Generate project name from directory name or use "root" for root package
        String projectName = relativePath.isEmpty() ? "root" : projectDir.getFileName().toString();
        
        // Create unique ID
        String projectId = generateSubProjectId(repositoryId, relativePath, "npm");
        
        SubProjectMetadata subProject = new SubProjectMetadata();
        subProject.setName(projectName);
        subProject.setPath(relativePath);
        subProject.setType("npm");
        // Set ID AFTER setting path and type to avoid auto-regeneration overriding it
        subProject.setId(projectId);
        subProject.setBuildFile(NPM_BUILD_FILE);
        subProject.setRepositoryId(repositoryId);
        subProject.setDetectedAt(detectionTime);
        
        // NPM projects typically have different source structure
        List<String> npmSourcePatterns = Arrays.asList("src", "lib", "index.js");
        List<String> npmTestPatterns = Arrays.asList("test", "tests", "__tests__");
        
        subProject.setSourceDirectories(detectSourceDirectories(projectDir, npmSourcePatterns));
        subProject.setTestDirectories(detectSourceDirectories(projectDir, npmTestPatterns));
        
        // TODO: Parse package.json to extract more metadata (dependencies, version, description)
        // For now, set basic info
        subProject.setDescription("NPM project detected at " + relativePath);
        
        return subProject;
    }
    
    /**
     * Detect existing source directories based on common patterns
     */
    private List<String> detectSourceDirectories(final Path projectDir, final List<String> patterns) {
        List<String> existingDirs = new ArrayList<>();
        
        for (String pattern : patterns) {
            Path sourceDir = projectDir.resolve(pattern);
            if (Files.exists(sourceDir) && Files.isDirectory(sourceDir)) {
                existingDirs.add(pattern);
            }
        }
        
        return existingDirs;
    }
    
    /**
     * Generate a unique ID for a sub-project
     */
    private String generateSubProjectId(final String repositoryId, final String relativePath, final String type) {
        String pathSuffix = relativePath.isEmpty() ? "root" : relativePath.replace("/", "_").replace("\\", "_");
        return repositoryId + "_" + type + "_" + pathSuffix;
    }
    
    /**
     * Check if a directory contains indicators of a specific project type
     */
    public boolean isProjectType(final Path directory, final String projectType) {
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return false;
        }
        
        switch (projectType.toLowerCase()) {
            case "maven":
                return Files.exists(directory.resolve(MAVEN_BUILD_FILE));
            case "gradle":
                return Files.exists(directory.resolve(GRADLE_BUILD_FILE)) || 
                       Files.exists(directory.resolve(GRADLE_KOTLIN_BUILD_FILE));
            case "npm":
                return Files.exists(directory.resolve(NPM_BUILD_FILE));
            default:
                return false;
        }
    }
    
    /**
     * Get the build file name for a project type
     */
    public String getBuildFileName(final String projectType) {
        if (projectType == null) {
            return null;
        }
        switch (projectType.toLowerCase()) {
            case "maven":
                return MAVEN_BUILD_FILE;
            case "gradle":
                return GRADLE_BUILD_FILE;
            case "npm":
                return NPM_BUILD_FILE;
            default:
                return null;
        }
    }
}
