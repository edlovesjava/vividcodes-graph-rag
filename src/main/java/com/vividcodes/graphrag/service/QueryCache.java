package com.vividcodes.graphrag.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.vividcodes.graphrag.model.dto.QueryResult;

@Component
public class QueryCache {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryCache.class);
    
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    // Cache configuration
    private static final int MAX_CACHE_SIZE = 1000;
    private static final long CACHE_TTL_SECONDS = 300; // 5 minutes
    
    public QueryCache() {
        // Schedule cache cleanup every minute
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, 60, 60, TimeUnit.SECONDS);
    }
    
    public QueryResult get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            LOGGER.debug("Cache hit for key: {}", key);
            return entry.getResult();
        }
        
        if (entry != null && entry.isExpired()) {
            cache.remove(key);
            LOGGER.debug("Removed expired cache entry for key: {}", key);
        }
        
        return null;
    }
    
    public void put(String key, QueryResult result) {
        if (cache.size() >= MAX_CACHE_SIZE) {
            cleanup(); // Remove expired entries
            if (cache.size() >= MAX_CACHE_SIZE) {
                // Remove oldest entry if still at max size
                String oldestKey = cache.keySet().iterator().next();
                cache.remove(oldestKey);
                LOGGER.debug("Removed oldest cache entry due to size limit: {}", oldestKey);
            }
        }
        
        cache.put(key, new CacheEntry(result, System.currentTimeMillis()));
        LOGGER.debug("Cached result for key: {}", key);
    }
    
    public void clear() {
        cache.clear();
        LOGGER.info("Query cache cleared");
    }
    
    public int size() {
        return cache.size();
    }
    
    private void cleanup() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        LOGGER.debug("Cache cleanup completed, size: {}", cache.size());
    }
    
    private static class CacheEntry {
        private final QueryResult result;
        private final long timestamp;
        
        public CacheEntry(QueryResult result, long timestamp) {
            this.result = result;
            this.timestamp = timestamp;
        }
        
        public QueryResult getResult() {
            return result;
        }
        
        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }
        
        public boolean isExpired(long currentTime) {
            return (currentTime - timestamp) > (CACHE_TTL_SECONDS * 1000);
        }
    }
}
