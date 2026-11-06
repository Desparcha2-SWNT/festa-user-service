# Arquitectura

`festa-user-service` sigue arquitectura hexagonal (puertos y adaptadores)
con Spring Boot.

## Capas

- **domain**: entidades, value objects, eventos in/out, excepciones, puertos.
  No conoce Spring, JPA, Rabbit, Redis ni HTTP.
- **application**: casos de uso, comandos, queries, caché (política), worker
  de outbox, handlers de eventos inbound. Orquesta dominio y puertos.
- **infrastructure**: adaptadores concretos (persistencia JPA, RabbitMQ,
  Redis, AES-GCM, cliente HTTP a auth).
- **api**: capa HTTP (Spring MVC), DTOs, seguridad, manejo global de errores.
- **config**: configuración de Spring (Security, Rabbit, Redis, Jackson, OpenAPI).

## Reglas de dependencia
api ──▶ application ──▶ domain ◀── infrastructure
▲
└── puertos (in/out)

text

- `domain` no importa nada de `application`, `infrastructure` ni `api`.
- `application` depende de `domain` (puertos y modelos) y de `config` (solo `AppProperties`).
- `infrastructure` implementa los puertos `out`.
- `api` depende de `application` (casos de uso) y usa `domain` solo a través de ellos.

## Límites con auth

| Concern | auth-service | user-service |
|---|---|---|
| Identidad / credenciales | dueño | nunca |
| `displayName`, `email` | dueño | no los guarda |
| `role` / `status` | dueño | los lee del JWT |
| Perfil extendido | no | dueño |
| Verificación de comercio | no | dueño |
| Consentimientos | no | dueño |
| Borrado Ley 1581 | inicia | consume `account.deleted` y borra su parte |

## Patrones usados

- **Transactional Outbox** en `outbox` para publicar eventos sin dual-write.
- **Inbox idempotente** en `processed_events(event_id, consumer)`.
- **JWKS remoto** delegado a `NimbusJwtDecoder` de Spring Security.