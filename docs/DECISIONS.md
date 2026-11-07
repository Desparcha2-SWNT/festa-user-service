# ADRs

## ADR-001: rol `merchant` solo tras verificación manual
`accountType=merchant` al registrarse no otorga el rol. Se otorga cuando un admin aprueba
la solicitud (PATCH `/auth/admin/accounts/{id}/role`). Refuerza RF3.

## ADR-002: `AuthAdminClient` reenvía el bearer del admin actuante
Sin infraestructura de service accounts en el piloto. Si el token expira, la transacción
revierte y el admin reintenta.

## ADR-003: `phone` y `address` del comercio cifrados
AES-256-GCM con `PII_ENCRYPTION_KEY`. El dominio los maneja en claro; el cifrado vive
en los mappers. `businessName`, categoría y coordenadas son públicos.

## ADR-004: JPA + mappers, no entidades de dominio anémicas
Entidades JPA viven en `infrastructure.persistence.entity`; el dominio usa records.
El costo de mapeo es aceptable por la separación.

## ADR-005: JWKS delegado a Spring Security
`user-service` es resource server, no emisor. `NimbusJwtDecoder` con `jwk-set-uri`
+ validador `iss=festa-auth`. Sin `JwksClientPort` propio.

## ADR-006: handlers de Rabbit sin `@Transactional` de método
Usan `InboxSupport.runOnce` (transacción programática) para hacer atómicos
`tryMarkProcessed` + mutación + `outbox.append`. Si algo falla, el mensaje
reintenta (no queda marcado en falso).

## ADR-007: rol `role` del JWT no autoriza recursos propios
Los endpoints `/users/me/**` autorizan por `sub == recurso`, no por claim `role`.
El claim solo se usa en `/users/admin/**` (con la ventana de staleness de 15 min
que ya documenta auth).

## ADR-008: `suspended` y `verificationStatus` son ortogonales
Una suspensión de moderación no destruye el estado de verificación. Visibilidad pública
= VERIFIED && !suspended.

## ADR-009: caché con política en `application`
`CachePort` en dominio; `RedisCacheAdapter` en infraestructura; claves y TTL en
`application.cache` (política del caso de uso, no del motor).

## ADR-010: sin tests en el entregable actual
Decisión del equipo de priorizar features. Los puertos y la estructura permiten
testear unitariamente cada caso de uso sin Spring.