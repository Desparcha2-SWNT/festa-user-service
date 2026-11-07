package com.festa.user.application.service;

import com.festa.user.application.cache.ProfileCacheService;
import com.festa.user.application.command.ReplaceInterestsCommand;
import com.festa.user.domain.event.outbound.ProfileUpdatedEvent;
import com.festa.user.domain.exception.NotFoundException;
import com.festa.user.domain.model.Profile;
import com.festa.user.domain.model.UserInterests;
import com.festa.user.domain.port.in.ReplaceInterestsUseCase;
import com.festa.user.domain.port.out.ClockPort;
import com.festa.user.domain.port.out.InterestRepositoryPort;
import com.festa.user.domain.port.out.OutboxRepositoryPort;
import com.festa.user.domain.port.out.ProfileRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterestsService implements ReplaceInterestsUseCase {

    private final InterestRepositoryPort interests;
    private final ProfileRepositoryPort profiles;
    private final OutboxRepositoryPort outbox;
    private final ClockPort clock;
    private final ProfileCacheService cache;

    public InterestsService(
            InterestRepositoryPort interests,
            ProfileRepositoryPort profiles,
            OutboxRepositoryPort outbox,
            ClockPort clock,
            ProfileCacheService cache
    ) {
        this.interests = interests;
        this.profiles = profiles;
        this.outbox = outbox;
        this.clock = clock;
        this.cache = cache;
    }

    @Override
    @Transactional
    public UserInterests handle(ReplaceInterestsCommand command) {
        Profile profile = profiles.findByAccountId(command.accountId())
                .orElseThrow(() -> NotFoundException.of("Perfil", command.accountId()));

        UserInterests current = interests.findByAccountId(command.accountId())
                .orElse(UserInterests.empty(command.accountId(), clock.now()));

        UserInterests replaced = current.replace(command.interests(), clock.now());
        UserInterests saved = interests.save(replaced);

        outbox.append(ProfileUpdatedEvent.of(
                command.accountId().value(), profile.displayName(), saved.interests(), clock.now()
        ));
        cache.evictAllForAccount(command.accountId());

        return saved;
    }
}