package com.festa.user.application.service;

import com.festa.user.application.cache.ProfileCacheService;
import com.festa.user.application.command.ApproveMerchantCommand;
import com.festa.user.application.command.RejectMerchantCommand;
import com.festa.user.domain.event.outbound.MerchantRejectedEvent;
import com.festa.user.domain.event.outbound.MerchantUpdatedEvent;
import com.festa.user.domain.event.outbound.MerchantVerifiedEvent;
import com.festa.user.domain.exception.NotFoundException;
import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.model.MerchantVerificationEvent;
import com.festa.user.domain.port.in.ApproveMerchantUseCase;
import com.festa.user.domain.port.in.ListPendingMerchantsUseCase;
import com.festa.user.domain.port.in.RejectMerchantUseCase;
import com.festa.user.domain.port.out.AuthAdminClientPort;
import com.festa.user.domain.port.out.ClockPort;
import com.festa.user.domain.port.out.MerchantRepositoryPort;
import com.festa.user.domain.port.out.MerchantVerificationEventRepositoryPort;
import com.festa.user.domain.port.out.OutboxRepositoryPort;
import com.festa.user.domain.vo.MerchantStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminMerchantService
        implements ListPendingMerchantsUseCase, ApproveMerchantUseCase, RejectMerchantUseCase {

    private final MerchantRepositoryPort merchants;
    private final MerchantVerificationEventRepositoryPort auditEvents;
    private final OutboxRepositoryPort outbox;
    private final AuthAdminClientPort authAdmin;
    private final ClockPort clock;
    private final ProfileCacheService cache;

    public AdminMerchantService(
            MerchantRepositoryPort merchants,
            MerchantVerificationEventRepositoryPort auditEvents,
            OutboxRepositoryPort outbox,
            AuthAdminClientPort authAdmin,
            ClockPort clock,
            ProfileCacheService cache
    ) {
        this.merchants = merchants;
        this.auditEvents = auditEvents;
        this.outbox = outbox;
        this.authAdmin = authAdmin;
        this.clock = clock;
        this.cache = cache;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MerchantProfile> handle() {
        return merchants.findByStatus(MerchantStatus.PENDING.name());
    }

    @Override
    @Transactional
    public MerchantProfile handle(ApproveMerchantCommand command) {
        MerchantProfile current = merchants.findByAccountId(command.targetAccountId())
                .orElseThrow(() -> NotFoundException.of("Perfil de comercio", command.targetAccountId()));

        MerchantStatus previous = current.verificationStatus();
        MerchantProfile approved = current.approve(clock.now());
        MerchantProfile saved = merchants.save(approved);

        auditEvents.save(MerchantVerificationEvent.of(
                saved.accountId(), previous, MerchantStatus.VERIFIED,
                command.actorId(), null, clock.now()
        ));

        // Efecto remoto: cambio de rol en auth. Si falla, revierte toda la transacción.
        authAdmin.changeRole(saved.accountId(), "merchant", command.adminBearerToken());

        outbox.append(MerchantVerifiedEvent.of(saved, command.actorId().value(), clock.now()));
        cache.evictAllForAccount(saved.accountId());

        return saved;
    }

    @Override
    @Transactional
    public MerchantProfile handle(RejectMerchantCommand command) {
        MerchantProfile current = merchants.findByAccountId(command.targetAccountId())
                .orElseThrow(() -> NotFoundException.of("Perfil de comercio", command.targetAccountId()));

        MerchantStatus previous = current.verificationStatus();
        MerchantProfile rejected = current.reject(command.reason(), clock.now());
        MerchantProfile saved = merchants.save(rejected);

        auditEvents.save(MerchantVerificationEvent.of(
                saved.accountId(), previous, MerchantStatus.REJECTED,
                command.actorId(), command.reason(), clock.now()
        ));

        // Si el merchant estaba VERIFIED (regresión), degradamos el rol en auth.
        if (previous == MerchantStatus.VERIFIED) {
            authAdmin.changeRole(saved.accountId(), "user", command.adminBearerToken());
            // event y payment no consumen merchant.rejected: así dejan de tratarlo como verificado.
            outbox.append(MerchantUpdatedEvent.of(saved, clock.now()));
        }

        outbox.append(MerchantRejectedEvent.of(saved, command.reason(), command.actorId().value(), clock.now()));
        cache.evictAllForAccount(saved.accountId());

        return saved;
    }
}