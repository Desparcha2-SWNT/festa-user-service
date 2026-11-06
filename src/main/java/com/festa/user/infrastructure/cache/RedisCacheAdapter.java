package com.festa.user.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.festa.user.domain.port.out.CachePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisCacheAdapter implements CachePort {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RedisCacheAdapter(StringRedisTemplate redis) {
        this.redis = redis;
        // ObjectMapper propio para no compartir configuración con la capa HTTP.
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String json = redis.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, type));
        } catch (Exception ex) {
            // Dato corrupto o cambio de esquema: se descarta.
            redis.delete(key);
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "No se pudo serializar el valor para cache key " + key, ex);
        }
    }

    @Override
    public void evict(String key) {
        redis.delete(key);
    }

    @Override
    public void evictPrefix(String prefix) {
        var keys = redis.keys(prefix);
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
    }
}