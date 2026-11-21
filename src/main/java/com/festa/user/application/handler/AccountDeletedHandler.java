package com.festa.user.application.handler;

import com.festa.user.domain.event.inbound.AccountDeletedEvent;
import com.festa.user.domain.port.out.ConsentRepositoryPort;
import com.festa.user.domain.port.out.InterestRepositoryPort;
import com.festa.user.domain.port.out.MerchantRepositoryPort;
import com.festa.user.domain.port.out.MerchantVerificationEventRepositoryPort;
import com.festa.user.domain.port.out.PreferencesRepositoryPort;
import com.festa.user.domain.port.out.ProfileRepositoryPort;
import com.festa.user.domain.port.out.ReportRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Ley 1581: borrado real de la PII propia. Se borra primero el merchant
 * (y sus audit events), luego consents, preferences, interests y por último
 * el profile. El CASCADE de BD lo haría igual, pero el orden explícito deja
 * clara la intención y no depende del motor.
 */
@Component
public class AccountDeletedHandler {

    private static final Logger log = LoggerFactory.getLogger(AccountDeletedHandler.class);
    public static final String CONSUMER = "user-service.account.deleted";

    private final InboxSupport inbox;
    private final ProfileRepositoryPort profiles;
    private final InterestRepositoryPort interests;
    private final PreferencesRepositoryPort preferences;
    private final ConsentRepositoryPort consents;
    private final MerchantRepositoryPort merchants;
    private final MerchantVerificationEventRepositoryPort auditEvents;
    private final ReportRepositoryPort reports;

    public AccountDeletedHandler(
            InboxSupport inbox,
            ProfileRepositoryPort profiles,
            InterestRepositoryPort interests,
            PreferencesRepositoryPort preferences,
            ConsentRepositoryPort consents,
            MerchantRepositoryPort merchants,
            MerchantVerificationEventRepositoryPort auditEvents,
            ReportRepositoryPort reports
    ) {
        this.inbox = inbox;
        this.profiles = profiles;
        this.interests = interests;
        this.preferences = preferences;
        this.consents = consents;
        this.merchants = merchants;
        this.auditEvents = auditEvents;
        this.reports = reports;
    }

    public void handle(AccountDeletedEvent event) {
        AccountId accountId = AccountId.of(event.accountId());
        inbox.runOnce(event.eventId(), CONSUMER, () -> {
            // RF14: los reportes que hizo (contrato account.deleted: "borra perfil, intereses y reportes hechos").
            reports.deleteByReporter(accountId.value());
            auditEvents.deleteByAccountId(accountId);
            merchants.deleteByAccountId(accountId);
            consents.deleteByAccountId(accountId);
            preferences.deleteByAccountId(accountId);
            interests.deleteByAccountId(accountId);
            profiles.deleteByAccountId(accountId);
            log.info("Datos personales eliminados para {}", accountId);
        });
    }
}