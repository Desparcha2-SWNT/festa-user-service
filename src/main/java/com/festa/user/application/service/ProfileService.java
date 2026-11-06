package com.festa.user.application.service;

import com.festa.user.application.cache.ProfileCacheService;
import com.festa.user.application.command.UpdateProfileCommand;
import com.festa.user.application.query.MyProfileView;
import com.festa.user.domain.event.outbound.UserProfileUpdatedEvent;
import com.festa.user.domain.exception.NotFoundException;
import com.festa.user.domain.model.Consent;
import com.festa.user.domain.model.Profile;
import com.festa.user.domain.model.UserInterests;
import com.festa.user.domain.model.UserPreferences;
import com.festa.user.domain.port.in.GetMyProfileUseCase;
import com.festa.user.domain.port.in.UpdateMyProfileUseCase;
import com.festa.user.domain.port.out.ClockPort;
import com.festa.user.domain.port.out.ConsentRepositoryPort;
import com.festa.user.domain.port.out.InterestRepositoryPort;
import com.festa.user.domain.port.out.MerchantRepositoryPort;
import com.festa.user.domain.port.out.OutboxRepositoryPort;
import com.festa.user.domain.port.out.PreferencesRepositoryPort;
import com.festa.user.domain.port.out.ProfileRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class ProfileService implements GetMyProfileUseCase, UpdateMyProfileUseCase {

    private final ProfileRepositoryPort profiles;
    private final InterestRepositoryPort interests;
    private final PreferencesRepositoryPort preferences;
    private final ConsentRepositoryPort consents;
    private final MerchantRepositoryPort merchants;
    private final OutboxRepositoryPort outbox;
    private final ClockPort clock;
    private final ProfileCacheService cache;

    public ProfileService(
            ProfileRepositoryPort profiles,
            InterestRepositoryPort interests,
            PreferencesRepositoryPort preferences,
            ConsentRepositoryPort consents,
            MerchantRepositoryPort merchants,
            OutboxRepositoryPort outbox,
            ClockPort clock,
            ProfileCacheService cache
    ) {
        this.profiles = profiles;
        this.interests = interests;
        this.preferences = preferences;
        this.consents = consents;
        this.merchants = merchants;
        this.outbox = outbox;
        this.clock = clock;
        this.cache = cache;
    }

    @Override
    @Transactional(readOnly = true)
    public MyProfileView handle(AccountId accountId) {
        Profile profile = profiles.findByAccountId(accountId)
                .orElseThrow(() -> NotFoundException.of("Perfil", accountId));

        UserInterests userInterests = interests.findByAccountId(accountId)
                .orElseGet(() -> UserInterests.empty(accountId, profile.updatedAt()));

        UserPreferences userPreferences = preferences.findByAccountId(accountId)
                .orElseGet(() -> UserPreferences.defaults(accountId, profile.updatedAt()));

        List<Consent> latestConsents = consents.findLatestPerType(accountId);

        Optional<MyProfileView.MerchantSummary> merchantSummary = merchants.findByAccountId(accountId)
                .map(m -> new MyProfileView.MerchantSummary(
                        m.businessName(),
                        m.verificationStatus(),
                        m.suspended(),
                        m.rejectionReason()
                ));

        return new MyProfileView(profile, userInterests, userPreferences, latestConsents, merchantSummary);
    }

    @Override
    @Transactional
    public Profile handle(UpdateProfileCommand command) {
        Profile current = profiles.findByAccountId(command.accountId())
                .orElseThrow(() -> NotFoundException.of("Perfil", command.accountId()));

        Set<String> changed = new HashSet<>();
        String newBio = current.bio();
        String newAvatar = current.avatarUrl();
        String newPhone = current.phone();

        if (command.bio() != null && !Objects.equals(command.bio(), current.bio())) {
            newBio = command.bio().isBlank() ? null : command.bio();
            changed.add("bio");
        }
        if (command.avatarUrl() != null && !Objects.equals(command.avatarUrl(), current.avatarUrl())) {
            newAvatar = command.avatarUrl().isBlank() ? null : command.avatarUrl();
            changed.add("avatarUrl");
        }
        if (command.phone() != null && !Objects.equals(command.phone(), current.phone())) {
            newPhone = command.phone().isBlank() ? null : command.phone();
            changed.add("phone");
        }

        if (changed.isEmpty()) {
            return current;
        }

        Profile updated = current.applyUpdate(newBio, newAvatar, newPhone, clock.now());
        Profile saved = profiles.save(updated);

        outbox.append(UserProfileUpdatedEvent.of(command.accountId().value(), changed, clock.now()));
        cache.evictAllForAccount(command.accountId());

        return saved;
    }
}