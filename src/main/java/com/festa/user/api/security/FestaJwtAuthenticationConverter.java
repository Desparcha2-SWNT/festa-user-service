package com.festa.user.api.security;

import com.festa.user.domain.vo.AccountId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

/**
 * Convierte el Jwt en JwtAuthenticationToken con FestaPrincipal y
 * authority ROLE_<role>. Falla si falta sub o role: no dejamos pasar
 * tokens incompletos.
 */
public class FestaJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String sub = jwt.getSubject();
        String role = jwt.getClaimAsString("role");
        if (sub == null || sub.isBlank()) {
            throw new IllegalStateException("JWT sin claim sub");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalStateException("JWT sin claim role");
        }
        FestaPrincipal principal = new FestaPrincipal(AccountId.of(sub), role);
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        return new JwtAuthenticationToken(jwt, authorities, principal.getName()) {
            @Override
            public Object getPrincipal() {
                return principal;
            }
        };
    }
}