package com.festa.user.application.service;

import com.festa.user.application.cache.ProfileCacheService;
import com.festa.user.application.command.RecordConsentCommand;
import com.festa.user.domain.event.outbound.UserConsentUpdatedEvent;
import com.festa.user.domain.exception.NotFoundException;
import com.festa.user.domain.model.Consent;
import com.festa.user.domain.port.in.ListConsentsUseCase;
import com.festa.user.domain.port.in.RecordConsentUseCase;
import com.festa.user.domain.port.out.ClockPort;
import com.festa.user.domain.port.out.ConsentRepositoryPort;
import com.festa.user.domain.port.out.OutboxRepositoryPort;
import com.festa.user.domain.port.out.ProfileRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsentsService implements ListConsentsUseCase, RecordConsentUseCase {

    private final ConsentRepositoryPort consents;
    private final ProfileRepositoryPort profiles;
    private final OutboxRepositoryPort outbox;
    private final ClockPort clock;
    private final ProfileCacheService cache;

    public ConsentsService(
            ConsentRepositoryPort consents,
            ProfileRepositoryPort profiles,
            OutboxRepositoryPort outbox,
            ClockPort clock,
            ProfileCacheService cache
    ) {
        this.consents = consents;
        this.profiles = profiles;
        this.outbox = outbox;
        this.clock = clock;
        this.cache = cache;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Consent> handle(AccountId accountId) {
        profiles.findByAccountId(accountId)
                .orElseThrow(() -> NotFoundException.of("Perfil", accountId));
        return consents.findLatestPerType(accountId);
    }

    @Override
    @Transactional
    public Consent handle(RecordConsentCommand command) {
        profiles.findByAccountId(command.accountId())
                .orElseThrow(() -> NotFoundException.of("Perfil", command.accountId()));

        Consent consent = Consent.record(
                command.accountId(),
                command.type(),
                command.granted(),
                command.version(),
                command.ipHash(),
                clock.now()
        );
        Consent saved = consents.save(consent);

        outbox.append(UserConsentUpdatedEvent.of(
                command.accountId().value(),
                saved.type(),
                saved.granted(),
                saved.version(),
                clock.now()
        ));
        cache.evictAllForAccount(command.accountId());

        return saved;
    }
}