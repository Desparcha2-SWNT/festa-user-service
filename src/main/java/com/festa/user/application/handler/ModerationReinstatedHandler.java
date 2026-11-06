package com.festa.user.application.handler;

import com.festa.user.domain.event.inbound.ModerationUserReinstatedEvent;
import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.port.out.ClockPort;
import com.festa.user.domain.port.out.MerchantRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ModerationReinstatedHandler {

    private static final Logger log = LoggerFactory.getLogger(ModerationReinstatedHandler.class);
    public static final String CONSUMER = "user-service.moderation.reinstated";

    private final InboxSupport inbox;
    private final MerchantRepositoryPort merchants;
    private final ClockPort clock;

    public ModerationReinstatedHandler(
            InboxSupport inbox,
            MerchantRepositoryPort merchants,
            ClockPort clock
    ) {
        this.inbox = inbox;
        this.merchants = merchants;
        this.clock = clock;
    }

    public void handle(ModerationUserReinstatedEvent event) {
        AccountId accountId = AccountId.of(event.accountId());
        inbox.runOnce(event.eventId(), CONSUMER, () -> {
            Optional<MerchantProfile> merchant = merchants.findByAccountId(accountId);
            if (merchant.isEmpty()) {
                return;
            }
            MerchantProfile m = merchant.get();
            if (!m.suspended()) {
                return;
            }
            merchants.save(m.markSuspended(false, clock.now()));
            log.info("Comercio {} visible de nuevo tras reintegro", accountId);
        });
    }
}