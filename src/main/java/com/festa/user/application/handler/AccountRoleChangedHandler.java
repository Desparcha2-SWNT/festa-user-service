package com.festa.user.application.handler;

import com.festa.user.domain.event.inbound.AccountRoleChangedEvent;
import com.festa.user.domain.exception.NotFoundException;
import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.port.out.ClockPort;
import com.festa.user.domain.port.out.MerchantRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.MerchantStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * auth publica este evento tras un cambio de rol. Si el usuario dejó de ser
 * merchant, el comercio deja de ser visible (suspended=true). Si vuelve a ser
 * merchant, se reactiva la visibilidad solo si la verificación sigue VERIFIED.
 * No se borra el perfil: el borrado real solo ocurre con account.deleted.
 */
@Component
public class AccountRoleChangedHandler {

    private static final Logger log = LoggerFactory.getLogger(AccountRoleChangedHandler.class);
    public static final String CONSUMER = "user-service.account.role_changed";

    private final InboxSupport inbox;
    private final MerchantRepositoryPort merchants;
    private final ClockPort clock;

    public AccountRoleChangedHandler(
            InboxSupport inbox,
            MerchantRepositoryPort merchants,
            ClockPort clock
    ) {
        this.inbox = inbox;
        this.merchants = merchants;
        this.clock = clock;
    }

    public void handle(AccountRoleChangedEvent event) {
        AccountId accountId = AccountId.of(event.accountId());
        inbox.runOnce(event.eventId(), CONSUMER, () -> {
            MerchantProfile merchant = merchants.findByAccountId(accountId)
                    .orElseThrow(() -> NotFoundException.of("Perfil de comercio", accountId));

            boolean wasMerchant = "merchant".equals(event.oldRole());
            boolean isMerchant = "merchant".equals(event.newRole());

            if (wasMerchant && !isMerchant) {
                if (merchant.verificationStatus() == MerchantStatus.VERIFIED && !merchant.suspended()) {
                    merchants.save(merchant.markSuspended(true, clock.now()));
                    log.info("Comercio {} desactivado por downgrade de rol", accountId);
                }
            } else if (!wasMerchant && isMerchant) {
                if (merchant.verificationStatus() == MerchantStatus.VERIFIED && merchant.suspended()) {
                    merchants.save(merchant.markSuspended(false, clock.now()));
                    log.info("Comercio {} reactivado por promoción de rol", accountId);
                }
            }
        });
    }
}