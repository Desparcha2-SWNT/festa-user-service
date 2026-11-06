package com.festa.user.application.handler;

import com.festa.user.domain.event.inbound.ModerationUserSuspendedEvent;
import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.port.out.ClockPort;
import com.festa.user.domain.port.out.MerchantRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ModerationSuspendedHandler {

    private static final Logger log = LoggerFactory.getLogger(ModerationSuspendedHandler.class);
    public static final String CONSUMER = "user-service.moderation.suspended";

    private final InboxSupport inbox;
    private final MerchantRepositoryPort merchants;
    private final ClockPort clock;

    public ModerationSuspendedHandler(
            InboxSupport inbox,
            MerchantRepositoryPort merchants,
            ClockPort clock
    ) {
        this.inbox = inbox;
        this.merchants = merchants;
        this.clock = clock;
    }

    public void handle(ModerationUserSuspendedEvent event) {
        AccountId accountId = AccountId.of(event.accountId());
        inbox.runOnce(event.eventId(), CONSUMER, () -> {
            Optional<MerchantProfile> merchant = merchants.findByAccountId(accountId);
            if (merchant.isEmpty()) {
                log.debug("Sin comercio para {}; nada que ocultar", accountId);
                return;
            }
            if (merchant.get().suspended()) {
                return;
            }
            merchants.save(merchant.get().markSuspended(true, clock.now()));
            log.info("Comercio {} ocultado por suspensión de cuenta", accountId);
        });
    }
}