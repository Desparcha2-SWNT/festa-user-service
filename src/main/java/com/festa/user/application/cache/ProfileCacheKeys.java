package com.festa.user.application.cache;

import com.festa.user.domain.vo.AccountId;

/**
 * Convención: user:{feature}:{accountId}. TTLs cortos (60s) porque
 * los datos cambian poco pero queremos consistencia eventual.
 * La invalidación se hace en cada escritura, así que el TTL es solo
 * la red de seguridad.
 */
public final class ProfileCacheKeys {

    private ProfileCacheKeys() {}

    public static String profile(AccountId accountId) {
        return "user:profile:" + accountId.value();
    }

    public static String interests(AccountId accountId) {
        return "user:interests:" + accountId.value();
    }

    public static String preferences(AccountId accountId) {
        return "user:preferences:" + accountId.value();
    }

    public static String consents(AccountId accountId) {
        return "user:consents:" + accountId.value();
    }

    public static String publicMerchant(AccountId accountId) {
        return "user:merchant:public:" + accountId.value();
    }

    public static String prefixForAccount(AccountId accountId) {
        return "user:*:" + accountId.value();
    }
}