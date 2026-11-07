package com.festa.user.api.mapper;

import com.festa.user.api.dto.response.ConsentResponse;
import com.festa.user.api.dto.response.InterestsResponse;
import com.festa.user.api.dto.response.MerchantResponse;
import com.festa.user.api.dto.response.PreferencesResponse;
import com.festa.user.api.dto.response.ProfileResponse;
import com.festa.user.api.dto.response.PublicMerchantResponse;
import com.festa.user.application.query.MyProfileView;
import com.festa.user.application.query.PublicMerchantView;
import com.festa.user.domain.model.Consent;
import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.model.UserInterests;
import com.festa.user.domain.model.UserPreferences;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    public ProfileResponse toProfileResponse(MyProfileView view) {
        var p = view.profile();
        var i = view.interests();
        var pref = view.preferences();

        List<ConsentResponse> consents = view.consents().stream()
                .map(this::toConsentResponse)
                .toList();

        MerchantResponse merchant = view.merchant()
                .map(m -> new MerchantResponse(
                        p.accountId().value(),
                        m.businessName(),
                        m.status().name(),
                        null,
                        0, 0,
                        null,
                        m.status().name(),
                        m.suspended(),
                        m.rejectionReason(),
                        p.createdAt(),
                        p.updatedAt()
                ))
                .orElse(null);

        return new ProfileResponse(
                p.accountId().value(),
                p.bio(),
                p.avatarUrl(),
                p.phone(),
                p.createdAt(),
                p.updatedAt(),
                i.interests().stream().map(Enum::name).collect(Collectors.toList()),
                toPreferencesResponse(pref),
                consents,
                merchant
        );
    }

    public PreferencesResponse toPreferencesResponse(UserPreferences pref) {
        return new PreferencesResponse(
                pref.budgetRange().name(),
                pref.preferredZones(),
                pref.preferredDays(),
                pref.maxDistanceKm(),
                pref.updatedAt()
        );
    }

    public ConsentResponse toConsentResponse(Consent c) {
        return new ConsentResponse(
                c.type().name(),
                c.granted(),
                c.version(),
                c.grantedAt()
        );
    }

    public InterestsResponse toInterestsResponse(UserInterests i) {
        return new InterestsResponse(
                i.interests().stream().map(Enum::name).collect(Collectors.toSet()),
                i.updatedAt()
        );
    }

    public MerchantResponse toMerchantResponse(MerchantProfile m) {
        return new MerchantResponse(
                m.accountId().value(),
                m.businessName(),
                m.category().name(),
                m.address(),
                m.latitude(),
                m.longitude(),
                m.description(),
                m.verificationStatus().name(),
                m.suspended(),
                m.rejectionReason(),
                m.createdAt(),
                m.updatedAt()
        );
    }

    public PublicMerchantResponse toPublicMerchantResponse(PublicMerchantView v) {
        return new PublicMerchantResponse(
                v.accountId().value(),
                v.businessName(),
                v.category().name(),
                v.address(),
                v.latitude(),
                v.longitude(),
                v.description()
        );
    }
}