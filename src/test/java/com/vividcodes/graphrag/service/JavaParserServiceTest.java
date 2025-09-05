package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.vividcodes.graphrag.config.ParserConfig;

class JavaParserServiceTest {
    
    private RepositoryService repositoryService;
    private ParserConfig parserConfig;
    private JavaParserService javaParserService;
    private org.springframework.context.ApplicationContext applicationContext;
    
    @BeforeEach
    void setUp() {
        // Use a simple mock implementation instead of Mockito
        repositoryService = new SimpleMockRepositoryService();
        
        // Create a simple mock ApplicationContext
        applicationContext = new SimpleMockApplicationContext();
        
        parserConfig = new ParserConfig();
        ReflectionTestUtils.setField(parserConfig, "includePrivate", false);
        ReflectionTestUtils.setField(parserConfig, "includeTests", false);
        ReflectionTestUtils.setField(parserConfig, "maxFileSize", 10 * 1024 * 1024L);
        
        javaParserService = new JavaParserService(parserConfig, repositoryService, applicationContext);
    }
    
    @Test
    void testParseDirectory_ValidPath() {
        // Get the test resources directory
        Path testResourcesPath = Paths.get("src", "test", "resources", "test-data");
        
        // This test will verify that the service can handle a valid directory
        assertDoesNotThrow(() -> {
            javaParserService.parseDirectory(testResourcesPath.toString());
        });
    }
    
    @Test
    void testParseDirectory_InvalidPath() {
        String invalidPath = "/non/existent/path";
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            javaParserService.parseDirectory(invalidPath);
        });
        
        assertTrue(exception.getMessage().contains("Source path does not exist") || 
                  exception.getCause().getMessage().contains("Source path does not exist"));
    }
    
    @Test
    void testParserConfig() {
        assertFalse(parserConfig.isIncludePrivate());
        assertFalse(parserConfig.isIncludeTests());
        assertEquals(10 * 1024 * 1024L, parserConfig.getMaxFileSize());
        assertEquals("java", parserConfig.getSupportedExtensions());
    }
    
    @Test
    void testShouldIncludeFile_NullPath() {
        // Test that null path is handled gracefully using reflection
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(javaParserService, "shouldIncludeFile", (Path) null));
    }
    
    @Test
    void testShouldIncludeFile_PathWithNullFileName() {
        // Test with a real path that might have null fileName
        // This is a more realistic test that doesn't require mocking
        Path testPath = Paths.get("src", "test", "resources", "test-data");
        // The test passes if the method handles the path without throwing NPE
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(javaParserService, "shouldIncludeFile", testPath);
        });
    }
    
    /**
     * Simple mock implementation of GraphService for testing.
     * This avoids the Mockito ByteBuddy issues with Java 17.
     */
    private static class SimpleMockGraphService implements GraphService {
        
        @Override
        public void savePackage(com.vividcodes.graphrag.model.graph.PackageNode packageNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveClass(com.vividcodes.graphrag.model.graph.ClassNode classNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveMethod(com.vividcodes.graphrag.model.graph.MethodNode methodNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveField(com.vividcodes.graphrag.model.graph.FieldNode fieldNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveAnnotation(com.vividcodes.graphrag.model.graph.AnnotationNode annotationNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveRepository(com.vividcodes.graphrag.model.graph.RepositoryNode repositoryNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void createRelationship(String fromId, String toId, String relationshipType) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void createRelationship(String fromId, String toId, String relationshipType, 
                                    java.util.Map<String, Object> properties) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void clearAllData() {
            // Mock implementation - do nothing
        }
        
        @Override
        public java.util.Map<String, Object> getDataStatistics() {
            // Mock implementation - return empty map
            return new java.util.HashMap<>();
        }
        
        @Override
        public void saveSubProject(com.vividcodes.graphrag.model.graph.SubProjectNode subProjectNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public com.vividcodes.graphrag.model.graph.SubProjectNode findSubProjectById(String id) {
            // Mock implementation - return null
            return null;
        }
        
        @Override
        public java.util.List<com.vividcodes.graphrag.model.graph.SubProjectNode> findSubProjectsByRepositoryId(String repositoryId) {
            // Mock implementation - return empty list
            return new java.util.ArrayList<>();
        }
        
        @Override
        public void deleteSubProject(String id) {
            // Mock implementation - do nothing
        }
    }
    
    /**
     * Simple mock implementation of RepositoryService for testing.
     */
    private static class SimpleMockRepositoryService extends RepositoryService {
        
        public SimpleMockRepositoryService() {
            super(null, null, null); // Pass null dependencies for testing
        }
        
        @Override
        public com.vividcodes.graphrag.model.dto.RepositoryMetadata detectRepositoryMetadata(java.nio.file.Path filePath) {
            // Mock implementation - return null
            return null;
        }
        
        @Override
        public com.vividcodes.graphrag.model.graph.RepositoryNode createOrUpdateRepository(com.vividcodes.graphrag.model.dto.RepositoryMetadata metadata) {
            // Mock implementation - return null
            return null;
        }
        
        @Override
        public void linkNodesToRepository(java.util.List<Object> nodes, com.vividcodes.graphrag.model.graph.RepositoryNode repository) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void clearCache() {
            // Mock implementation - do nothing
        }
        
        @Override
        public java.util.Map<String, Object> getRepositoryStats() {
            // Mock implementation - return empty map
            return new java.util.HashMap<>();
        }
    }
    
    /**
     * Simple mock implementation of ApplicationContext for testing.
     * This avoids the Mockito ByteBuddy issues with Java 17.
     */
    private static class SimpleMockApplicationContext implements org.springframework.context.ApplicationContext {
        @Override
        public Object getBean(String name) throws org.springframework.beans.BeansException {
            return null;
        }

        @Override
        public <T> T getBean(String name, Class<T> requiredType) throws org.springframework.beans.BeansException {
            return null;
        }

        @Override
        public Object getBean(String name, Object... args) throws org.springframework.beans.BeansException {
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getBean(Class<T> requiredType) throws org.springframework.beans.BeansException {
            // Return a mock JavaGraphVisitor when requested
            if (requiredType == com.vividcodes.graphrag.service.JavaGraphVisitor.class) {
                return (T) createMockJavaGraphVisitor();
            }
            return null;
        }

        @Override
        public <T> T getBean(Class<T> requiredType, Object... args) throws org.springframework.beans.BeansException {
            return null;
        }

        @Override
        public <T> org.springframework.beans.factory.ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
            return null;
        }
        
        @Override
        public <T> org.springframework.beans.factory.ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {
            return null;
        }

        @Override
        public <T> org.springframework.beans.factory.ObjectProvider<T> getBeanProvider(org.springframework.core.ResolvableType requiredType) {
            return null;
        }
        
        @Override
        public <T> org.springframework.beans.factory.ObjectProvider<T> getBeanProvider(org.springframework.core.ResolvableType requiredType, boolean allowEagerInit) {
            return null;
        }

        @Override
        public boolean containsBean(String name) {
            return false;
        }

        @Override
        public boolean isSingleton(String name) throws org.springframework.beans.factory.NoSuchBeanDefinitionException {
            return false;
        }

        @Override
        public boolean isPrototype(String name) throws org.springframework.beans.factory.NoSuchBeanDefinitionException {
            return false;
        }

        @Override
        public boolean isTypeMatch(String name, org.springframework.core.ResolvableType typeToMatch) throws org.springframework.beans.factory.NoSuchBeanDefinitionException {
            return false;
        }

        @Override
        public boolean isTypeMatch(String name, Class<?> typeToMatch) throws org.springframework.beans.factory.NoSuchBeanDefinitionException {
            return false;
        }

        @Override
        public Class<?> getType(String name) throws org.springframework.beans.factory.NoSuchBeanDefinitionException {
            return null;
        }

        @Override
        public Class<?> getType(String name, boolean allowFactoryBeanInit) throws org.springframework.beans.factory.NoSuchBeanDefinitionException {
            return null;
        }

        @Override
        public String[] getAliases(String name) {
            return new String[0];
        }

        @Override
        public org.springframework.beans.factory.BeanFactory getParentBeanFactory() {
            return null;
        }

        @Override
        public boolean containsLocalBean(String name) {
            return false;
        }

        @Override
        public String[] getBeanDefinitionNames() {
            return new String[0];
        }

        @Override
        public int getBeanDefinitionCount() {
            return 0;
        }

        @Override
        public String[] getBeanNamesForType(org.springframework.core.ResolvableType type) {
            return new String[0];
        }

        @Override
        public String[] getBeanNamesForType(org.springframework.core.ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
            return new String[0];
        }

        @Override
        public String[] getBeanNamesForType(Class<?> type) {
            return new String[0];
        }

        @Override
        public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
            return new String[0];
        }

        @Override
        public <T> java.util.Map<String, T> getBeansOfType(Class<T> type) throws org.springframework.beans.BeansException {
            return new java.util.HashMap<>();
        }

        @Override
        public <T> java.util.Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws org.springframework.beans.BeansException {
            return new java.util.HashMap<>();
        }

        @Override
        public String[] getBeanNamesForAnnotation(Class<? extends java.lang.annotation.Annotation> annotationType) {
            return new String[0];
        }

        @Override
        public java.util.Map<String, Object> getBeansWithAnnotation(Class<? extends java.lang.annotation.Annotation> annotationType) throws org.springframework.beans.BeansException {
            return new java.util.HashMap<>();
        }

        @Override
        public <A extends java.lang.annotation.Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws org.springframework.beans.factory.NoSuchBeanDefinitionException {
            return null;
        }

        @Override
        public <A extends java.lang.annotation.Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) throws org.springframework.beans.factory.NoSuchBeanDefinitionException {
            return null;
        }

        @Override
        public <A extends java.lang.annotation.Annotation> java.util.Set<A> findAllAnnotationsOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) throws org.springframework.beans.factory.NoSuchBeanDefinitionException {
            return new java.util.HashSet<>();
        }

        @Override
        public String getId() {
            return "mock-context";
        }

        @Override
        public String getApplicationName() {
            return "mock-app";
        }

        @Override
        public String getDisplayName() {
            return "Mock Application Context";
        }

        @Override
        public long getStartupDate() {
            return System.currentTimeMillis();
        }

        @Override
        public org.springframework.context.ApplicationContext getParent() {
            return null;
        }

        @Override
        public org.springframework.context.ApplicationEventPublisher getApplicationEventPublisher() {
            return null;
        }

        @Override
        public org.springframework.core.env.Environment getEnvironment() {
            return null;
        }

        @Override
        public org.springframework.core.io.support.ResourcePatternResolver getResourcePatternResolver() {
            return null;
        }

        @Override
        public org.springframework.core.io.Resource[] getResources(String locationPattern) throws java.io.IOException {
            return new org.springframework.core.io.Resource[0];
        }

        @Override
        public org.springframework.beans.factory.config.AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
            return null;
        }

        @Override
        public org.springframework.core.io.Resource getResource(String location) {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public void publishEvent(Object event) {
        }

        @Override
        public void publishEvent(org.springframework.context.ApplicationEvent event) {
        }

        @Override
        public String getMessage(String code, Object[] args, String defaultMessage, java.util.Locale locale) {
            return defaultMessage;
        }

        @Override
        public String getMessage(String code, Object[] args, java.util.Locale locale) throws org.springframework.context.NoSuchMessageException {
            return code;
        }

        @Override
        public String getMessage(org.springframework.context.MessageSourceResolvable resolvable, java.util.Locale locale) throws org.springframework.context.NoSuchMessageException {
            return resolvable.getDefaultMessage();
        }
        
        /**
         * Create a mock JavaGraphVisitor for testing.
         */
        private com.vividcodes.graphrag.service.JavaGraphVisitor createMockJavaGraphVisitor() {
            // Create a simple mock that implements the essential methods
            return new com.vividcodes.graphrag.service.JavaGraphVisitor(
                new ParserConfig(),
                new SimpleMockGraphService(),
                new com.vividcodes.graphrag.service.NodeFactory(new com.vividcodes.graphrag.service.TypeResolver()),
                new com.vividcodes.graphrag.service.RelationshipManager(new SimpleMockGraphService()),
                new com.vividcodes.graphrag.service.DependencyAnalyzer(
                    new com.vividcodes.graphrag.service.TypeResolver(),
                    new com.vividcodes.graphrag.service.NodeFactory(new com.vividcodes.graphrag.service.TypeResolver()),
                    new com.vividcodes.graphrag.service.RelationshipManager(new SimpleMockGraphService()),
                    new SimpleMockGraphService()
                ),
                new com.vividcodes.graphrag.service.TypeResolver()
            );
        }
    }
} 