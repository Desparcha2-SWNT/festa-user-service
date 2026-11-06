# Eventos

## Consume — `user-service.inbox`

| Evento | Publisher | Efecto |
|---|---|---|
| `account.registered` | auth | Crea profile + interests + preferences vacíos |
| `account.role_changed` | auth | Ajusta visibilidad del comercio (suspende/reactiva) |
| `account.deleted` | auth | Borra toda la PII propia (Ley 1581) |
| `moderation.user_suspended` | moderation | Oculta la ficha pública del comercio |
| `moderation.user_reinstated` | moderation | Reactiva visibilidad si sigue VERIFIED |

Colas: `user-service.inbox` → `.retry` (30s TTL) → `.dlq`.

Idempotencia: `processed_events(event_id, consumer)` en la misma transacción que la mutación.

## Publica — `festa.user`

| Evento | Payload | Consumidores |
|---|---|---|
| `user.profile_updated` | `accountId`, `changedFields` | Analytics |
| `user.preferences_updated` | `accountId`, `budgetRange`, `preferredZones`, `preferredDays`, `maxDistanceKm` | AI Service |
| `user.interests_updated` | `accountId`, `interests` | AI Service |
| `user.consent_updated` | `accountId`, `consentType`, `granted`, `version` | AI Service, Analytics |
| `merchant.verification_requested` | `accountId`, `businessName`, `category`, `submittedAt` | Analytics |
| `merchant.verified` | `accountId`, `businessName`, `category`, `approvedBy`, `approvedAt` | Event Service, Analytics |
| `merchant.rejected` | `accountId`, `businessName`, `reason`, `rejectedBy`, `rejectedAt` | Analytics |

Publicación: transactional outbox + publisher confirms. Worker dedicado (`OutboxPublisherWorker`).