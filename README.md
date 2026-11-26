# festa-user-service

Microservicio de usuarios de Festa: perfil extendido, intereses, preferencias,
consentimientos (Ley 1581) y verificación manual de comercios (RF3).

- **Stack**: Java 21 · Spring Boot 3.5 · Postgres · RabbitMQ · Redis
- **Puerto**: 8082 (el gateway expone `/api/users/...`)
- **Auth**: JWT RS256 emitido por `festa-auth-service`, validado con la llave pública de `JWT_PUBLIC_KEY_PATH`

## Qué hace

- Guarda perfil extendido (bio, avatar, teléfono cifrado con AES-256-GCM).
- Guarda intereses y preferencias de plan (RF2); los intereses salen en `profile.updated`.
- Registra consentimientos granulares con versión y fecha (RNF5, RN6).
- Recibe solicitudes de perfil de comercio y las somete a verificación manual (RF3).
- Publica eventos de dominio por outbox (`festa.user`), según `festa-infra/contracts`.
- Consume `account.registered` y `account.deleted` de auth (`user-service.inbox`).

Detalle de eventos en [`docs/EVENTS.md`](docs/EVENTS.md).

## Qué NO hace

- Credenciales, JWT, refresh tokens, sesiones → `festa-auth-service`.
- Roles como fuente de verdad → auth. Aquí solo se leen del JWT.
- Eventos, mapa, booking, pagos, IA, notificaciones → sus servicios respectivos.

## Correr

```bash
# Con Docker (desde festa-infra)
docker compose --profile user up -d --build

# Local (requiere festa-infra arriba y .env cargado)
cp .env.example .env
set -a; source .env; set +a
mvn spring-boot:run

# Pruebas (ContractTest usa ../festa-infra/contracts si existe)
mvn test

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

Prometheus: `GET /metrics` (lo raspa `festa-infra/prometheus`). Health: `GET /health`.



# Guía de Pruebas End-to-End: `festa-user-service`

Esta guía detalla los escenarios de prueba para validar el microservicio de usuarios, incluyendo su integración con el `auth-service` (JWT y roles), la base de datos PostgreSQL, RabbitMQ y Redis.

## 🛠️ Configuración Previa en Postman

Antes de empezar, asegúrate de tener configurado un *Environment* en Postman con las siguientes variables:

| Variable | Valor Inicial | Descripción |
| :--- | :--- | :--- |
| `user_base` | `http://localhost:8082` | URL del microservicio de usuarios |
| `auth_base` | `http://localhost:8081` | URL del microservicio de autenticación |
| `token` | *(vacío)* | Token JWT de un usuario normal |
| `admin_token` | *(vacío)* | Token JWT de un usuario con rol `admin` |
| `account_id` | *(vacío)* | UUID del usuario registrado |
| `merchant_id` | *(vacío)* | UUID del comercio (usualmente el mismo `account_id`) |

En las peticiones que requieran autenticación, agrega el header:
`Authorization: Bearer {{token}}` o `Authorization: Bearer {{admin_token}}` según corresponda.

---

## 1. Flujo de Perfil (RF2)
*Validación de la creación del perfil asíncrono (vía evento) y actualización de datos personales.*

### 1.1 Obtener mi perfil vacío (Recién registrado)
- **Método:** `GET`
- **URL:** `{{user_base}}/users/me`
- **Headers:** `Authorization: Bearer {{token}}`
- **Status Esperado:** `200 OK`
- **Output Esperado:**
```json
{
  "accountId": "tu-uuid-de-usuario",
  "bio": null,
  "avatarUrl": null,
  "phone": null,
  "createdAt": "2026-05-20T10:00:00Z",
  "updatedAt": "2026-05-20T10:00:00Z",
  "interests": [],
  "preferences": {
    "budgetRange": "ANY",
    "preferredZones": [],
    "preferredDays": [],
    "maxDistanceKm": null,
    "updatedAt": "2026-05-20T10:00:00Z"
  },
  "consents": [],
  "merchant": null
}
```

### 1.2 Actualizar perfil (PATCH)
- **Método:** `PATCH`
- **URL:** `{{user_base}}/users/me`
- **Headers:** `Authorization: Bearer {{token}}`
- **Body (JSON):**
```json
{
  "bio": "Amante del café",
  "phone": "+573001234567"
}
```
- **Status Esperado:** `200 OK`
- **Output Esperado:** (Fíjate que `bio` y `phone` ya tienen valor y `updatedAt` cambió)
```json
{
  "accountId": "tu-uuid-de-usuario",
  "bio": "Amante del café",
  "avatarUrl": null,
  "phone": "+573001234567",
  "createdAt": "2026-05-20T10:00:00Z",
  "updatedAt": "2026-05-20T10:05:00Z",
  "interests": [],
  "preferences": { ... },
  "consents": [],
  "merchant": null
}
```

---

## 2. Flujo de Preferencias y Consentimientos (RF2, RNF5)
*Validación de intereses, preferencias de plan para la IA y consentimientos informados (Ley 1581).*

### 2.1 Guardar Intereses
- **Método:** `PUT`
- **URL:** `{{user_base}}/users/me/interests`
- **Headers:** `Authorization: Bearer {{token}}`
- **Body (JSON):**
```json
{
  "interests": ["MUSICAL", "GASTRONOMICO"]
}
```
- **Status Esperado:** `200 OK`
- **Output Esperado:**
```json
{
  "interests": ["MUSICAL", "GASTRONOMICO"],
  "updatedAt": "2026-05-20T10:06:00Z"
}
```

### 2.2 Guardar Preferencias
- **Método:** `PUT`
- **URL:** `{{user_base}}/users/me/preferences`
- **Headers:** `Authorization: Bearer {{token}}`
- **Body (JSON):**
```json
{
  "budgetRange": "MEDIUM",
  "preferredZones": ["ECI-Norte"],
  "preferredDays": ["FRIDAY"],
  "maxDistanceKm": 10
}
```
- **Status Esperado:** `200 OK`
- **Output Esperado:**
```json
{
  "budgetRange": "MEDIUM",
  "preferredZones": ["ECI-Norte"],
  "preferredDays": ["FRIDAY"],
  "maxDistanceKm": 10,
  "updatedAt": "2026-05-20T10:07:00Z"
}
```

### 2.3 Registrar Consentimiento (Ej. Ubicación)
- **Método:** `POST`
- **URL:** `{{user_base}}/users/me/consents`
- **Headers:** `Authorization: Bearer {{token}}`
- **Body (JSON):**
```json
{
  "type": "LOCATION",
  "granted": true,
  "version": "2026-01"
}
```
- **Status Esperado:** `200 OK`
- **Output Esperado:**
```json
{
  "type": "LOCATION",
  "granted": true,
  "version": "2026-01",
  "grantedAt": "2026-05-20T10:08:00Z"
}
```

---

## 3. Flujo de Comercio (RF3)
*Validación de la verificación manual, el cambio de rol en Auth y la publicación de eventos.*

### 3.1 Enviar solicitud de comercio (Usuario normal)
- **Método:** `POST`
- **URL:** `{{user_base}}/users/me/merchant`
- **Headers:** `Authorization: Bearer {{token}}`
- **Body (JSON):**
```json
{
  "businessName": "Café Central",
  "category": "GASTRONOMICO",
  "address": "Calle 1 # 2-3",
  "latitude": 4.6,
  "longitude": -74.0,
  "description": "Café de especialidad"
}
```
- **Status Esperado:** `201 Created`
- **Output Esperado:**
```json
{
  "accountId": "tu-uuid-de-usuario",
  "businessName": "Café Central",
  "category": "GASTRONOMICO",
  "address": "Calle 1 # 2-3",
  "latitude": 4.6,
  "longitude": -74.0,
  "description": "Café de especialidad",
  "verificationStatus": "PENDING",
  "suspended": false,
  "rejectionReason": null,
  "createdAt": "2026-05-20T10:10:00Z",
  "updatedAt": "2026-05-20T10:10:00Z"
}
```

### 3.2 Ver la cola de pendientes (Admin)
- **Método:** `GET`
- **URL:** `{{user_base}}/users/admin/merchants`
- **Headers:** `Authorization: Bearer {{admin_token}}`
- **Status Esperado:** `200 OK`
- **Output Esperado:** Un array con el comercio creado.
```json
[
  {
    "accountId": "tu-uuid-de-usuario",
    "businessName": "Café Central",
    "verificationStatus": "PENDING",
    ...
  }
]
```

### 3.3 Aprobar el comercio (Admin)
- **Método:** `POST`
- **URL:** `{{user_base}}/users/admin/merchants/{{account_id}}/approve`
- **Headers:** `Authorization: Bearer {{admin_token}}`
- **Status Esperado:** `200 OK`
- **Output Esperado:** El perfil con estado `VERIFIED`.
```json
{
  "accountId": "tu-uuid-de-usuario",
  "businessName": "Café Central",
  "verificationStatus": "VERIFIED",
  "suspended": false,
  ...
}
```
> **Nota:** Detrás de cámaras, el `user-service` llamó al `auth-service` para ascender tu rol a `merchant`. Si haces un `POST /auth/refresh` en auth con tu token original, el nuevo token dirá `"role": "merchant"`.

### 3.4 Ver ficha pública (Sin token)
- **Método:** `GET`
- **URL:** `{{user_base}}/users/merchants/{{account_id}}`
- **Headers:** *(Ninguno)*
- **Status Esperado:** `200 OK`
- **Output Esperado:**
```json
{
  "accountId": "tu-uuid-de-usuario",
  "businessName": "Café Central",
  "category": "GASTRONOMICO",
  "address": "Calle 1 # 2-3",
  "latitude": 4.6,
  "longitude": -74.0,
  "description": "Café de especialidad"
}
```

---

## 4. Flujo de Errores y Seguridad (RNF4, RNF11)
*Verificación de la protección de datos, validaciones y roles.*

### 4.1 Acceso sin token
- **Método:** `GET`
- **URL:** `{{user_base}}/users/me`
- **Headers:** *(Ninguno)*
- **Status Esperado:** `401 Unauthorized`

### 4.2 Usuario normal intentando entrar a Admin
- **Método:** `GET`
- **URL:** `{{user_base}}/users/admin/merchants`
- **Headers:** `Authorization: Bearer {{token}}` (Token de rol `user`)
- **Status Esperado:** `403 Forbidden`

### 4.3 Validación de campos del formulario (RNF11)
- **Método:** `POST`
- **URL:** `{{user_base}}/users/me/merchant`
- **Headers:** `Authorization: Bearer {{token}}`
- **Body (JSON):**
```json
{
  "businessName": "",
  "category": "GASTRONOMICO",
  "address": "x",
  "latitude": 999,
  "longitude": 0
}
```
- **Status Esperado:** `400 Bad Request`
- **Output Esperado:**
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Algunos campos no cumplen las validaciones",
    "details": [
      { "field": "businessName", "message": "no debe estar vacío" },
      { "field": "latitude", "message": "debe ser menor o igual a 90.0" }
    ]
  }
}
```

---

## 5. Flujo de Borrado (Ley 1581)
*Comprobación de la eliminación de la PII (Información Personal Identificable).*

### 5.1 Eliminar cuenta (en Auth)
- **Método:** `DELETE`
- **URL:** `{{auth_base}}/auth/me` *(¡Ojo, apunta al puerto de Auth!)*
- **Headers:** `Authorization: Bearer {{token}}`
- **Status Esperado:** `204 No Content`

### 5.2 Verificar que el perfil ya no existe (en User)
- **Método:** `GET`
- **URL:** `{{user_base}}/users/me`
- **Headers:** `Authorization: Bearer {{token}}` (Si el token no ha expirado)
- **Status Esperado:** `404 Not Found`
- **Output Esperado:**
```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Perfil no encontrado: tu-uuid-de-usuario"
  }
}
```

---

## 🌟 Bonus: Verificación de Eventos Asíncronos y Métricas
Para estar 100% seguro de que el patrón *Outbox* y RabbitMQ funcionan:

- **Método:** `GET`
- **URL:** `{{user_base}}/metrics`

Busca en el texto devuelto las siguientes métricas (los valores deben ir aumentando):
- `festa_outbox_published_total`: Eventos que se enviaron a RabbitMQ.
- `festa_outbox_pending`: Debería ser 0 si todo fluye rápido.
- `festa_messages_consumed_total`: Número de eventos de auth procesados (por `type` y `outcome`).

## Reportes y moderación (RF14)

En el piloto la moderación es manual: cualquier usuario reporta y un admin decide. Cada decisión se publica en
`festa.user` y la aplican los demás servicios (auth bloquea el login y revoca refresh tokens, event oculta los
eventos, notification avisa).

| Método y ruta | Quién | Qué hace / publica |
|---|---|---|
| `POST /reports` `{targetType: event\|user, targetId, reason, comment?}` | Usuario | Crea el reporte (`report.created`). 409 si ya tiene uno abierto sobre lo mismo |
| `GET /reports/mine` | Usuario | Sus reportes y su estado |
| `GET /users/admin/reports` | Admin | Reportes abiertos |
| `POST /users/admin/reports/{id}/resolve` `{outcome: dismissed\|action_taken, action?: hide_event\|suspend_user, reason?, suspendedUntil?}` | Admin | `report.resolved` y, si hay acción, `moderation.event_hidden` o `moderation.user_suspended` |
| `POST /users/admin/moderation/events/{eventId}/hide` `{reason}` / `.../restore` | Admin | `moderation.event_hidden` / `moderation.event_restored` |
| `POST /users/admin/moderation/users/{userId}/suspend` `{reason, until?}` / `.../reinstate` | Admin | `moderation.user_suspended` / `moderation.user_reinstated` |

Motivos válidos: `spam`, `fraude`, `contenido_inapropiado`, `informacion_falsa`, `acoso`, `otro`. Con
`account.deleted` se borran los reportes que hizo el usuario (Ley 1581).
