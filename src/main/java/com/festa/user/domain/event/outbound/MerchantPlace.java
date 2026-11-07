package com.festa.user.domain.event.outbound;

import com.festa.user.domain.model.MerchantProfile;

/** Objeto `place` de merchant.verified y merchant.updated. */
public record MerchantPlace(String name, String address, Location location) {

    public record Location(double lat, double lng) {
    }

    public static MerchantPlace of(MerchantProfile m) {
        return new MerchantPlace(m.businessName(), m.address(), new Location(m.latitude(), m.longitude()));
    }
}
