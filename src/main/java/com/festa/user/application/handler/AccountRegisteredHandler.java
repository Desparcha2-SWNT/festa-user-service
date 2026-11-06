package com.festa.user.application.handler;

import com.festa.user.domain.event.inbound.AccountRegisteredEvent;
import com.festa.user.domain.model.Profile;
import com.festa.user.domain.model.UserInterests;
import com.festa.user.domain.model.UserPreferences;
import com.festa.user.domain.port.out.ClockPort;
import com.festa.user.domain.port.out.InterestRepositoryPort;
import com.festa.user.domain.port.out.PreferencesRepositoryPort;
import com.festa.user.domain.port.out.ProfileRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Crea el perfil vacío del usuario al registrarse. Idempotente por eventId
 * y defensivo: si el perfil ya existe (crash antes del ack), no falla.
 */
@Component
public class AccountRegisteredHandler {

    private static final Logger log = LoggerFactory.getLogger(AccountRegisteredHandler.class);
    public static final String CONSUMER = "user-service.account.registered";

    private final InboxSupport inbox;
    private final ProfileRepositoryPort profiles;
    private final InterestRepositoryPort interests;
    private final PreferencesRepositoryPort preferences;
    private final ClockPort clock;

    public AccountRegisteredHandler(
            InboxSupport inbox,
            ProfileRepositoryPort profiles,
            InterestRepositoryPort interests,
            PreferencesRepositoryPort preferences,
            ClockPort clock
    ) {
        this.inbox = inbox;
        this.profiles = profiles;
        this.interests = interests;
        this.preferences = preferences;
        this.clock = clock;
    }

    public void handle(AccountRegisteredEvent event) {
        AccountId accountId = AccountId.of(event.accountId());
        inbox.runOnce(event.eventId(), CONSUMER, () -> {
            if (profiles.findByAccountId(accountId).isPresent()) {
                log.debug("Perfil ya existe para {}, se omite", accountId);
                return;
            }
            Instant now = clock.now();
            profiles.save(Profile.emptyFor(accountId, now));
            interests.save(UserInterests.empty(accountId, now));
            preferences.save(UserPreferences.defaults(accountId, now));
            log.info("Perfil creado para {} (accountType={})", accountId, event.accountType());
        });
    }
}