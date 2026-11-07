# Eventos

La fuente de verdad es `festa-infra/contracts` (esquemas, sobre y `rabbitmq/definitions.json`).
Si user-service necesita publicar o consumir algo nuevo, primero se agrega allá.
`ContractTest` verifica este servicio contra esos contratos (`-Dfesta.contracts.dir`).

Todo mensaje va en el sobre del contrato: `eventId`, `eventType`, `version`, `occurredAt`,
`producer` (`user-service`), `correlationId` (el `X-Correlation-ID` de Kong), `causationId` y `data`.

## Consume — `user-service.inbox`

| Evento | Publisher | Efecto |
|---|---|---|
| `account.registered` | auth-service | Crea profile (con el `displayName` cifrado), interests y preferences vacíos |
| `account.deleted` | auth-service | Borra toda la PII propia (Ley 1581) |

- Un solo consumidor (`InboxMessageListener`) que despacha por `eventType` y hace ack manual.
- Sobre inválido, tipo no consumido o productor que no es el dueño: va directo a la DLQ.
- Error transitorio: se republica en `user-service.inbox.retry` (TTL 10 s) con `x-retry-count + 1`;
  desde `x-retry-count >= 3` va a `user-service.inbox.dlq` (por `festa.dlx`).
- Idempotencia: `processed_events(event_id, consumer)` en la misma transacción que la mutación.

## Publica — `festa.user`

| Evento | Cuándo | `data` |
|---|---|---|
| `profile.updated` | Cambian los intereses | `userId`, `displayName`, `interests`, `updatedAt` |
| `location_consent.granted` | Se acepta el consentimiento `LOCATION` | `userId`, `policyVersion`, `grantedAt` |
| `location_consent.revoked` | Se retira el consentimiento `LOCATION` | `userId`, `revokedAt` |
| `merchant.submitted` | Se envía o reenvía el comercio a verificación | `merchantId`, `userId`, `businessName`, `category`, `submittedAt` |
| `merchant.verified` | Un admin aprueba el comercio | `merchantId`, `userId`, `businessName`, `category`, `place`, `verifiedBy`, `verifiedAt` |
| `merchant.rejected` | Un admin rechaza el comercio | `merchantId`, `userId`, `reason`, `rejectedBy`, `rejectedAt` |
| `merchant.updated` | Un comercio verificado pierde la verificación (cambio crítico o rechazo) | `merchantId`, `userId`, `businessName`, `category`, `place`, `verified`, `updatedAt` |

- Hay un comercio por cuenta, así que `merchantId = userId`.
- Categorías e intereses van en minúscula (`gastronomico`, `otro`, ...).
- Bio, avatar, teléfono, preferencias y los demás consentimientos no tienen contrato: no se publican.

Publicación: transactional outbox + publisher confirms (`OutboxPublisherWorker`); si el broker no
confirma, el evento sigue pendiente en la outbox y se reintenta.
