package com.festa.user.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.festa.user.application.handler.AccountRegisteredHandler;
import com.festa.user.domain.event.inbound.AccountRegisteredEvent;
import com.festa.user.domain.port.out.AuthAdminClientPort;
import com.festa.user.domain.vo.AccountId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integración con PostgreSQL, RabbitMQ y Redis reales (Testcontainers): perfil creado por account.registered,
 * edición del perfil e intereses, y el ciclo de verificación manual de comercios con su outbox.
 * El token de prueba es "cuenta.rol" (TestSecurityConfig); el cambio de rol en auth-service se simula.
 */
class UserServiceIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    AccountRegisteredHandler registered;

    @Autowired
    JdbcTemplate jdbc;

    @MockitoBean
    AuthAdminClientPort authAdmin;

    private UUID register(String displayName) {
        UUID account = UUID.randomUUID();
        registered.handle(new AccountRegisteredEvent(
                UUID.randomUUID(), Instant.now(), "account.registered", account, displayName, "user", Instant.now()));
        return account;
    }

    // Token de prueba "cuenta.rol" (TestSecurityConfig): el punto sí es válido en un Bearer, los dos puntos no.
    private static String bearer(UUID account, String role) {
        return "Bearer " + account + "." + role;
    }

    private int outboxCount(String eventType) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM outbox WHERE event_type = ?", Integer.class, eventType);
    }

    private void submitMerchant(UUID account) throws Exception {
        mvc.perform(post("/users/me/merchant")
                        .header("Authorization", bearer(account, "user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName": "Café La Escuela", "category": "GASTRONOMICO",
                                 "address": "Calle 205 # 59-00, Bogotá", "latitude": 4.7826, "longitude": -74.0424,
                                 "description": "Café con música en vivo los viernes"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));
    }

    @Test
    void account_registered_crea_el_perfil_una_sola_vez() throws Exception {
        UUID account = UUID.randomUUID();
        var event = new AccountRegisteredEvent(
                UUID.randomUUID(), Instant.now(), "account.registered", account, "Ana", "user", Instant.now());

        registered.handle(event);
        registered.handle(event); // misma entrega repetida: la inbox la descarta

        mvc.perform(get("/users/me").header("Authorization", bearer(account, "user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(account.toString()))
                .andExpect(jsonPath("$.interests").isEmpty());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM profiles WHERE account_id = ?", Integer.class, account))
                .isEqualTo(1);
    }

    @Test
    void el_perfil_y_los_intereses_se_guardan_en_la_base() throws Exception {
        UUID account = register("Ana");

        mvc.perform(patch("/users/me")
                        .header("Authorization", bearer(account, "user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\": \"Me gustan los conciertos\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/users/me/interests")
                        .header("Authorization", bearer(account, "user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"interests\": [\"MUSICAL\", \"CULTURAL\"]}"))
                .andExpect(status().isOk());

        mvc.perform(get("/users/me").header("Authorization", bearer(account, "user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Me gustan los conciertos"))
                .andExpect(jsonPath("$.interests.length()").value(2));
    }

    @Test
    void aprobar_un_comercio_lo_verifica_cambia_el_rol_y_deja_merchant_verified_en_el_outbox() throws Exception {
        UUID merchant = register("Café La Escuela");
        UUID admin = UUID.randomUUID();
        submitMerchant(merchant);
        int before = outboxCount("merchant.verified");

        mvc.perform(get("/users/admin/merchants").header("Authorization", bearer(admin, "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == '" + merchant + "')]").exists());

        mvc.perform(post("/users/admin/merchants/{id}/approve", merchant)
                        .header("Authorization", bearer(admin, "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));

        verify(authAdmin).changeRole(eq(AccountId.of(merchant)), eq("merchant"), any());
        assertThat(outboxCount("merchant.verified")).isEqualTo(before + 1);
        mvc.perform(get("/users/merchants/{id}", merchant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Café La Escuela"));
    }

    @Test
    void si_auth_service_falla_la_aprobacion_se_revierte_completa() throws Exception {
        UUID merchant = register("Bar del Norte");
        UUID admin = UUID.randomUUID();
        submitMerchant(merchant);
        int before = outboxCount("merchant.verified");
        doThrow(new IllegalStateException("auth-service no responde"))
                .when(authAdmin).changeRole(eq(AccountId.of(merchant)), eq("merchant"), any());

        mvc.perform(post("/users/admin/merchants/{id}/approve", merchant)
                .header("Authorization", bearer(admin, "admin")));

        // La transacción se revierte: el comercio sigue pendiente y no sale ningún merchant.verified.
        mvc.perform(get("/users/me/merchant").header("Authorization", bearer(merchant, "user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));
        assertThat(outboxCount("merchant.verified")).isEqualTo(before);
    }

    @Test
    void rechazar_un_comercio_guarda_el_motivo_sin_tocar_el_rol() throws Exception {
        UUID merchant = register("Tienda Sur");
        UUID admin = UUID.randomUUID();
        submitMerchant(merchant);

        mvc.perform(post("/users/admin/merchants/{id}/reject", merchant)
                        .header("Authorization", bearer(admin, "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"La dirección no coincide con el RUT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("La dirección no coincide con el RUT"));

        verify(authAdmin, never()).changeRole(eq(AccountId.of(merchant)), any(), any());
    }

    @Test
    void solo_un_admin_revisa_comercios() throws Exception {
        UUID user = register("Ana");

        mvc.perform(get("/users/admin/merchants")).andExpect(status().isUnauthorized());
        mvc.perform(get("/users/admin/merchants").header("Authorization", bearer(user, "user")))
                .andExpect(status().isForbidden());
    }
}
