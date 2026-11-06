# festa-user-service

Microservicio de usuarios de Festa: perfil extendido, intereses, preferencias,
consentimientos (Ley 1581) y verificación manual de comercios (RF3).

- **Stack**: Java 21 · Spring Boot 3.5 · Postgres · RabbitMQ · Redis
- **Puerto**: 8082 (el gateway expone `/api/users/...`)
- **Auth**: JWT RS256 emitido por `festa-auth-service` (JWKS en `/.well-known/jwks.json`)

## Qué hace

- Guarda perfil extendido (bio, avatar, teléfono cifrado con AES-256-GCM).
- Guarda intereses y preferencias de plan (RF2) y los publica para AI/Analytics.
- Registra consentimientos granulares con versión y fecha (RNF5, RN6).
- Recibe solicitudes de perfil de comercio y las somete a verificación manual (RF3).
- Publica eventos de dominio por outbox (`festa.user`).
- Consume eventos de auth y moderación (`user-service.inbox`).

## Qué NO hace

- Credenciales, JWT, refresh tokens, sesiones → `festa-auth-service`.
- Roles como fuente de verdad → auth. Aquí solo se leen del JWT.
- Eventos, mapa, booking, pagos, IA, notificaciones → sus servicios respectivos.

## Correr

```bash
# Con Docker (desde festa-infra)
docker compose --profile user up -d --build

# Local (requiere infra arriba y .env cargado)
cp .env.example .env
source .env
./mvnw spring-boot:run

# Prueba de humo
./scripts/smoke.sh http://localhost:8082
```

## Documentación

- `docs/ARCHITECTURE.md`: hexagonal, límites, decisiones.
- `docs/API.md`: superficie HTTP.
- `docs/EVENTS.md`: eventos consumidos y publicados.
- `docs/DECISIONS.md`: ADRs resumidos.
- `openapi/openapi.yaml`: contrato OpenAPI 3.1.

## Métricas

- `festa_user_requests_total{action,outcome}`
- `festa_outbox_pending`
- `festa_outbox_published_total`
- `festa_messages_consumed_total{type}`

Prometheus: `GET /actuator/prometheus`.