package com.vividcodes.graphrag.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vividcodes.graphrag.model.dto.RepositoryMetadata;

@Service
public class GitService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GitService.class);
    
    /**
     * Check if a path is a Git repository
     */
    public boolean isGitRepository(Path path) {
        Path gitDir = path.resolve(".git");
        return Files.exists(gitDir) && Files.isDirectory(gitDir);
    }
    
    /**
     * Find the Git repository root for a given file path
     */
    public Optional<Path> findGitRepositoryRoot(Path filePath) {
        Path current = filePath.toAbsolutePath();
        
        // Walk up the directory tree looking for .git
        while (current != null && Files.exists(current)) {
            if (isGitRepository(current)) {
                return Optional.of(current);
            }
            current = current.getParent();
        }
        
        return Optional.empty();
    }
    
    /**
     * Extract repository name from path
     */
    public String extractRepositoryName(Path repositoryPath) {
        String pathName = repositoryPath.getFileName().toString();
        
        // If it's a standard git repository name, use it
        if (pathName.endsWith(".git")) {
            return pathName.substring(0, pathName.length() - 4);
        }
        
        return pathName;
    }
    
    /**
     * Get current branch
     */
    public String getCurrentBranch(Path repositoryPath) {
        try {
            Path headFile = repositoryPath.resolve(".git").resolve("HEAD");
            if (Files.exists(headFile)) {
                String headContent = Files.readString(headFile).trim();
                if (headContent.startsWith("ref: refs/heads/")) {
                    return headContent.substring(16);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Could not read current branch for repository: {}", repositoryPath, e);
        }
        
        return "unknown";
    }
    
    /**
     * Get last commit hash
     */
    public String getLastCommitHash(Path repositoryPath) {
        try {
            Path headFile = repositoryPath.resolve(".git").resolve("HEAD");
            if (Files.exists(headFile)) {
                String headContent = Files.readString(headFile).trim();
                if (headContent.startsWith("ref: ")) {
                    // Follow the ref
                    String refPath = headContent.substring(5);
                    Path refFile = repositoryPath.resolve(".git").resolve(refPath);
                    if (Files.exists(refFile)) {
                        return Files.readString(refFile).trim();
                    }
                } else {
                    // Direct commit hash
                    return headContent;
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Could not read last commit hash for repository: {}", repositoryPath, e);
        }
        
        return "unknown";
    }
    
    /**
     * Get commit date using git command
     */
    public LocalDateTime getCommitDate(Path repositoryPath) {
        try {
            String commitHash = getLastCommitHash(repositoryPath);
            if (!"unknown".equals(commitHash)) {
                ProcessBuilder pb = new ProcessBuilder("git", "show", "-s", "--format=%ci", commitHash);
                pb.directory(repositoryPath.toFile());
                
                Process process = pb.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String dateStr = reader.readLine();
                    if (dateStr != null) {
                        // Parse ISO date format: 2023-12-01 10:30:45 +0000
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
                        return LocalDateTime.parse(dateStr, formatter);
                    }
                }
                
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    LOGGER.warn("Git command failed with exit code: {}", exitCode);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not get commit date for repository: {}", repositoryPath, e);
        }
        
        return LocalDateTime.now();
    }
    
    /**
     * Get repository remote URL
     */
    public String getRemoteUrl(Path repositoryPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "config", "--get", "remote.origin.url");
            pb.directory(repositoryPath.toFile());
            
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String url = reader.readLine();
                if (url != null) {
                    return url.trim();
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOGGER.warn("Git config command failed with exit code: {}", exitCode);
            }
        } catch (Exception e) {
            LOGGER.warn("Could not get remote URL for repository: {}", repositoryPath, e);
        }
        
        return "unknown";
    }
    
    /**
     * Extract organization from remote URL
     */
    public String extractOrganization(String remoteUrl) {
        if (remoteUrl == null || "unknown".equals(remoteUrl)) {
            return "unknown";
        }
        
        // Handle different URL formats
        if (remoteUrl.contains("github.com/")) {
            String[] parts = remoteUrl.split("github\\.com/");
            if (parts.length > 1) {
                String[] orgRepo = parts[1].split("/");
                if (orgRepo.length > 0) {
                    return orgRepo[0];
                }
            }
        } else if (remoteUrl.contains("gitlab.com/")) {
            String[] parts = remoteUrl.split("gitlab\\.com/");
            if (parts.length > 1) {
                String[] orgRepo = parts[1].split("/");
                if (orgRepo.length > 0) {
                    return orgRepo[0];
                }
            }
        }
        
        return "unknown";
    }
    
    /**
     * Create repository metadata for a file
     */
    public RepositoryMetadata createRepositoryMetadata(Path filePath) {
        Optional<Path> repoRoot = findGitRepositoryRoot(filePath);
        
        if (repoRoot.isPresent()) {
            Path repositoryPath = repoRoot.get();
            String repoName = extractRepositoryName(repositoryPath);
            String remoteUrl = getRemoteUrl(repositoryPath);
            String organization = extractOrganization(remoteUrl);
            String branch = getCurrentBranch(repositoryPath);
            String commitHash = getLastCommitHash(repositoryPath);
            LocalDateTime commitDate = getCommitDate(repositoryPath);
            
            // Calculate relative path from repository root
            String relativePath = repositoryPath.relativize(filePath).toString();
            
            RepositoryMetadata metadata = new RepositoryMetadata(repoName, repositoryPath.toString());
            metadata.setRepositoryUrl(remoteUrl);
            metadata.setOrganization(organization);
            metadata.setBranch(branch);
            metadata.setCommitHash(commitHash);
            metadata.setCommitDate(commitDate);
            metadata.setFileRelativePath(relativePath);
            
            LOGGER.debug("Created repository metadata for file {}: repo={}, org={}, branch={}", 
                        filePath, repoName, organization, branch);
            
            return metadata;
        } else {
            // Not in a git repository
            LOGGER.debug("File {} is not in a git repository", filePath);
            return null;
        }
    }
}
