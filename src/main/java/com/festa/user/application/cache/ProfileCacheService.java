package com.festa.user.application.cache;

import com.festa.user.domain.port.out.CachePort;
import com.festa.user.domain.vo.AccountId;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Envoltorio delgado sobre CachePort con claves y TTLs fijos.
 * Los servicios de aplicación lo usan; los controllers nunca.
 */
@Component
public class ProfileCacheService {

    private static final Duration TTL = Duration.ofSeconds(60);

    private final CachePort cache;

    public ProfileCacheService(CachePort cache) {
        this.cache = cache;
    }

    public <T> T get(String key, Class<T> type) {
        return cache.get(key, type).orElse(null);
    }

    public void put(String key, Object value) {
        cache.put(key, value, TTL);
    }

    public void evict(String key) {
        cache.evict(key);
    }

    public void evictAllForAccount(AccountId accountId) {
        cache.evict(ProfileCacheKeys.profile(accountId));
        cache.evict(ProfileCacheKeys.interests(accountId));
        cache.evict(ProfileCacheKeys.preferences(accountId));
        cache.evict(ProfileCacheKeys.consents(accountId));
        cache.evict(ProfileCacheKeys.publicMerchant(accountId));
    }
}