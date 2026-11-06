package com.festa.user.application.query;

import com.festa.user.domain.model.Consent;
import com.festa.user.domain.model.Profile;
import com.festa.user.domain.model.UserInterests;
import com.festa.user.domain.model.UserPreferences;
import com.festa.user.domain.vo.MerchantStatus;

import java.util.List;
import java.util.Optional;

/**
 * Vista compuesta para GET /users/me.
 * merchantStatus es opcional: solo existe si la cuenta tiene perfil de comercio.
 * No incluye email ni displayName: esos los aporta auth desde su propio endpoint.
 */
public record MyProfileView(
        Profile profile,
        UserInterests interests,
        UserPreferences preferences,
        List<Consent> consents,
        Optional<MerchantSummary> merchant
) {

    public record MerchantSummary(
            String businessName,
            MerchantStatus status,
            boolean suspended,
            String rejectionReason
    ) {
    }
}