package com.festa.user.config;

import com.festa.user.api.security.FestaJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String AUTH_ISSUER = "festa-auth";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/merchants/**").permitAll()
                        .requestMatchers("/users/admin/**").hasRole("ADMIN")
                        .requestMatchers("/users/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new FestaJwtAuthenticationConverter())
                        )
                );
        return http.build();
    }

    // CORRECCIÓN AQUÍ: Se inyecta AppProperties correctamente como Bean
    @Bean
    public JwtDecoder jwtDecoder(AppProperties props) {
        // Si por alguna razón props es null (fallback), usamos el valor por defecto
        String baseUrl = (props != null && props.authAdmin() != null)
                ? props.authAdmin().baseUrl()
                : "http://localhost:8081";

        String jwkSetUri = baseUrl + "/.well-known/jwks.json";

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                issuerValidator()
        ));
        return decoder;
    }

    private OAuth2TokenValidator<Jwt> issuerValidator() {
        return jwt -> {
            String iss = jwt.getClaimAsString("iss");
            if (AUTH_ISSUER.equals(iss)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "issuer inválido", null
            ));
        };
    }
}