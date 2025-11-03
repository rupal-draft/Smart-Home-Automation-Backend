package com.shas.smart_home_automation_system.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void put(String cacheName, String key, Object value, long ttl, TimeUnit timeUnit) {
        String cacheKey = generateKey(cacheName, key);
        try {
            redisTemplate.opsForValue().set(cacheKey, value, ttl, timeUnit);
            log.debug("Cached data for key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Error caching data for key: {}", cacheKey, e);
        }
    }

    public <T> T get(String cacheName, String key, Class<T> type) {
        String cacheKey = generateKey(cacheName, key);
        try {
            Object value = redisTemplate.opsForValue().get(cacheKey);
            if (value != null) {
                log.debug("Cache hit for key: {}", cacheKey);
                return type.cast(value);
            }
        } catch (Exception e) {
            log.error("Error retrieving cached data for key: {}", cacheKey, e);
        }
        log.debug("Cache miss for key: {}", cacheKey);
        return null;
    }

    public void evict(String cacheName, String key) {
        String cacheKey = generateKey(cacheName, key);
        try {
            redisTemplate.delete(cacheKey);
            log.debug("Evicted cache for key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Error evicting cache for key: {}", cacheKey, e);
        }
    }

    public void evictPattern(String cacheName, String pattern) {
        try {
            String keyPattern = generateKey(cacheName, pattern) + "*";
            redisTemplate.delete(redisTemplate.keys(keyPattern));
            log.debug("Evicted cache for pattern: {}", keyPattern);
        } catch (Exception e) {
            log.error("Error evicting cache for pattern: {}", pattern, e);
        }
    }

    public boolean exists(String cacheName, String key) {
        String cacheKey = generateKey(cacheName, key);
        try {
            return redisTemplate.hasKey(cacheKey);
        } catch (Exception e) {
            log.error("Error checking cache existence for key: {}", cacheKey, e);
            return false;
        }
    }

    private String generateKey(String cacheName, String key) {
        return String.format("smarthome:%s:%s", cacheName, key);
    }
}
