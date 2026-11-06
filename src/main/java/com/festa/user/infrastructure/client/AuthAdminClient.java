package com.festa.user.infrastructure.client;

import com.festa.user.config.AppProperties;
import com.festa.user.domain.exception.ForbiddenException;
import com.festa.user.domain.exception.NotFoundException;
import com.festa.user.domain.exception.UnauthorizedException;
import com.festa.user.domain.port.out.AuthAdminClientPort;
import com.festa.user.domain.vo.AccountId;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Reenvía el bearer del admin actuante al PATCH /auth/admin/accounts/{id}/role.
 * Mapea respuestas a excepciones de dominio para que el servicio de aplicación
 * revierta la transacción.
 */
@Component
public class AuthAdminClient implements AuthAdminClientPort {

    private final RestClient restClient;
    private final AppProperties props;

    public AuthAdminClient(RestClient.Builder builder, AppProperties props) {
        this.props = props;
        this.restClient = builder.baseUrl(props.authAdmin().baseUrl()).build();
    }

    @Override
    public void changeRole(AccountId accountId, String newRole, String adminBearerToken) {
        String path = props.authAdmin().roleEndpoint()
                .replace("{id}", accountId.value().toString());

        var response = restClient.patch()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminBearerToken)
                .body(Map.of("role", newRole))
                .exchange((req, res) -> res.getStatusCode());

        if (response.is2xxSuccessful()) {
            return;
        }
        if (response.value() == HttpStatus.UNAUTHORIZED.value()) {
            throw new UnauthorizedException(
                    "El token del administrador expiró o no es válido");
        }
        if (response.value() == HttpStatus.FORBIDDEN.value()) {
            throw new ForbiddenException(
                    "El administrador no tiene permiso para cambiar roles");
        }
        if (response.value() == HttpStatus.NOT_FOUND.value()) {
            throw NotFoundException.of("Cuenta en auth", accountId);
        }
        throw new IllegalStateException(
                "auth respondió " + response.value() + " al cambiar el rol de " + accountId
        );
    }
}