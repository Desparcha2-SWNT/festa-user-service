package com.festa.user.application.service;

import com.festa.user.application.cache.ProfileCacheService;
import com.festa.user.application.command.SubmitMerchantCommand;
import com.festa.user.application.command.UpdateMerchantCommand;
import com.festa.user.domain.event.outbound.MerchantSubmittedEvent;
import com.festa.user.domain.event.outbound.MerchantUpdatedEvent;
import com.festa.user.domain.exception.ConflictException;
import com.festa.user.domain.exception.NotFoundException;
import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.model.MerchantVerificationEvent;
import com.festa.user.domain.port.in.GetMyMerchantUseCase;
import com.festa.user.domain.port.in.SubmitMerchantUseCase;
import com.festa.user.domain.port.in.UpdateMerchantUseCase;
import com.festa.user.domain.port.out.ClockPort;
import com.festa.user.domain.port.out.MerchantRepositoryPort;
import com.festa.user.domain.port.out.MerchantVerificationEventRepositoryPort;
import com.festa.user.domain.port.out.OutboxRepositoryPort;
import com.festa.user.domain.port.out.ProfileRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.MerchantStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService implements SubmitMerchantUseCase, GetMyMerchantUseCase, UpdateMerchantUseCase {

    private final MerchantRepositoryPort merchants;
    private final MerchantVerificationEventRepositoryPort auditEvents;
    private final ProfileRepositoryPort profiles;
    private final OutboxRepositoryPort outbox;
    private final ClockPort clock;
    private final ProfileCacheService cache;

    public MerchantService(
            MerchantRepositoryPort merchants,
            MerchantVerificationEventRepositoryPort auditEvents,
            ProfileRepositoryPort profiles,
            OutboxRepositoryPort outbox,
            ClockPort clock,
            ProfileCacheService cache
    ) {
        this.merchants = merchants;
        this.auditEvents = auditEvents;
        this.profiles = profiles;
        this.outbox = outbox;
        this.clock = clock;
        this.cache = cache;
    }

    @Override
    @Transactional
    public MerchantProfile handle(SubmitMerchantCommand command) {
        profiles.findByAccountId(command.accountId())
                .orElseThrow(() -> NotFoundException.of("Perfil", command.accountId()));

        if (merchants.findByAccountId(command.accountId()).isPresent()) {
            throw ConflictException.merchantAlreadyExists();
        }

        MerchantProfile submitted = MerchantProfile.submit(
                command.accountId(),
                command.businessName(),
                command.category(),
                command.address(),
                command.latitude(),
                command.longitude(),
                command.description(),
                clock.now()
        );
        MerchantProfile saved = merchants.save(submitted);

        auditEvents.save(MerchantVerificationEvent.of(
                saved.accountId(), null, MerchantStatus.PENDING, saved.accountId(), null, clock.now()
        ));

        outbox.append(MerchantSubmittedEvent.of(saved, clock.now()));
        cache.evictAllForAccount(command.accountId());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantProfile handle(AccountId accountId) {
        return merchants.findByAccountId(accountId)
                .orElseThrow(() -> NotFoundException.of("Perfil de comercio", accountId));
    }

    @Override
    @Transactional
    public MerchantProfile handle(UpdateMerchantCommand command) {
        MerchantProfile current = merchants.findByAccountId(command.accountId())
                .orElseThrow(() -> NotFoundException.of("Perfil de comercio", command.accountId()));

        MerchantStatus previous = current.verificationStatus();

        MerchantProfile updated = current.applyUpdate(
                command.businessName(),
                command.category(),
                command.address(),
                command.latitude(),
                command.longitude(),
                command.description(),
                clock.now()
        );
        MerchantProfile saved = merchants.save(updated);

        if (previous != saved.verificationStatus()) {
            auditEvents.save(MerchantVerificationEvent.of(
                    saved.accountId(), previous, saved.verificationStatus(),
                    saved.accountId(), null, clock.now()
            ));
            if (previous == MerchantStatus.VERIFIED) {
                // Perdió la verificación por un cambio crítico: event y payment deben enterarse.
                outbox.append(MerchantUpdatedEvent.of(saved, clock.now()));
            }
            if (saved.verificationStatus() == MerchantStatus.PENDING) {
                outbox.append(MerchantSubmittedEvent.of(saved, clock.now()));
            }
        }

        cache.evictAllForAccount(command.accountId());
        return saved;
    }
}