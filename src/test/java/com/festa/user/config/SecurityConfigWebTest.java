package com.festa.user.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.festa.user.api.controller.InterestsController;
import com.festa.user.api.mapper.DtoMapper;
import com.festa.user.domain.port.in.ReplaceInterestsUseCase;
import com.festa.user.integration.TestKeys;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * La cadena de seguridad real: CSRF sigue activo, pero oauth2ResourceServer lo ignora en
 * peticiones con token Bearer, así que la app (que siempre manda Bearer) puede modificar datos.
 */
@WebMvcTest(InterestsController.class)
@Import({SecurityConfig.class, SecurityConfigWebTest.Metrics.class})
class SecurityConfigWebTest {

    private static final String BODY = "{\"interests\":[\"MUSICAL\"]}";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ReplaceInterestsUseCase replaceInterests;

    @MockitoBean
    private DtoMapper mapper;

    @DynamicPropertySource
    static void jwt(DynamicPropertyRegistry registry) {
        registry.add("festa.user.jwt.public-key-path", TestKeys::publicKeyPem);
        registry.add("festa.user.jwt.issuer", () -> TestKeys.ISSUER);
    }

    @Test
    void putConTokenBearerFuncionaSinTokenCsrf() throws Exception {
        mvc.perform(put("/users/me/interests")
                        .header("Authorization", "Bearer " + TestKeys.token(UUID.randomUUID(), "user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk());
        verify(replaceInterests).handle(any());
    }

    @Test
    void putSinTokenNoLlegaAlControlador() throws Exception {
        mvc.perform(put("/users/me/interests").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().is4xxClientError());
        verifyNoInteractions(replaceInterests);
    }

    @Test
    void putConTokenDeOtraLlaveEs401() throws Exception {
        String forged = TestKeys.token(TestKeys.generate(), TestKeys.ISSUER, UUID.randomUUID(), "admin");
        mvc.perform(put("/users/me/interests")
                        .header("Authorization", "Bearer " + forged)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(replaceInterests);
    }

    @TestConfiguration
    static class Metrics {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }
}
