package com.festa.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.festa.user.application.cache.ProfileCacheService;
import com.festa.user.application.service.AdminMerchantService;
import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.event.outbound.MerchantUpdatedEvent;
import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.port.out.AuthAdminClientPort;
import com.festa.user.domain.port.out.MerchantRepositoryPort;
import com.festa.user.domain.port.out.MerchantVerificationEventRepositoryPort;
import com.festa.user.domain.port.out.OutboxRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.BusinessCategory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Republicar los comercios verificados: un merchant.updated por comercio, con sus datos actuales. */
class RepublishVerifiedMerchantsTest {

    private final MerchantRepositoryPort merchants = mock(MerchantRepositoryPort.class);
    private final OutboxRepositoryPort outbox = mock(OutboxRepositoryPort.class);
    private final Instant approvedAt = Instant.parse("2026-11-20T15:00:00Z");
    private final AdminMerchantService service = new AdminMerchantService(
            merchants, mock(MerchantVerificationEventRepositoryPort.class), outbox,
            mock(AuthAdminClientPort.class), () -> Instant.parse("2026-11-26T12:00:00Z"), mock(ProfileCacheService.class));

    private MerchantProfile verified(String name) {
        return MerchantProfile.submit(AccountId.of(UUID.randomUUID()), name, BusinessCategory.GASTRONOMICO,
                "Calle 205 # 59-00, Bogotá", 4.78, -74.04, null, approvedAt.minusSeconds(3600)).approve(approvedAt);
    }

    @Test
    void publica_merchant_updated_verificado_por_cada_comercio_con_su_fecha_de_aprobacion() {
        MerchantProfile cafe = verified("Café La Escuela");
        MerchantProfile bar = verified("Bar del Norte");
        when(merchants.findByStatus("VERIFIED")).thenReturn(List.of(cafe, bar));

        int count = service.republish();

        assertThat(count).isEqualTo(2);
        ArgumentCaptor<DomainEvent> sent = ArgumentCaptor.forClass(DomainEvent.class);
        verify(outbox, times(2)).append(sent.capture());
        assertThat(sent.getAllValues()).allSatisfy(e -> {
            assertThat(e).isInstanceOf(MerchantUpdatedEvent.class);
            MerchantUpdatedEvent u = (MerchantUpdatedEvent) e;
            assertThat(u.verified()).isTrue();
            // updatedAt es el del comercio, no la hora de la republicación: un consumidor que ya lo tiene no cambia.
            assertThat(u.updatedAt()).isEqualTo(approvedAt);
        });
        assertThat(sent.getAllValues()).extracting(e -> ((MerchantUpdatedEvent) e).businessName())
                .containsExactly("Café La Escuela", "Bar del Norte");
    }

    @Test
    void sin_comercios_verificados_no_publica_nada() {
        when(merchants.findByStatus("VERIFIED")).thenReturn(List.of());

        assertThat(service.republish()).isZero();
        verify(outbox, never()).append(any());
    }
}
