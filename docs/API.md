# API

Base: `/api/users` en el gateway. Directo: `http://user-service:8082/users`.

Formato de error:

```json
{ "error": { "code": "VALIDATION_ERROR", "message": "...", "details": [{"field":"x","message":"y"}] } }
```

| Método | Ruta | Auth | Qué hace |
|---|---|---|---|
| GET | `/users/me` | JWT | Perfil completo (perfil + intereses + preferencias + consentimientos + estado comercio) |
| PATCH | `/users/me` | JWT | Actualiza bio/avatar/phone. `null`=no cambiar, `""`=limpiar |
| PUT | `/users/me/interests` | JWT | Reemplaza intereses |
| GET | `/users/me/preferences` | JWT | Preferencias |
| PUT | `/users/me/preferences` | JWT | Actualiza preferencias |
| GET | `/users/me/consents` | JWT | Último consentimiento por tipo |
| POST | `/users/me/consents` | JWT | Registra consentimiento (con versión) |
| POST | `/users/me/merchant` | JWT | Envía solicitud de comercio (201) |
| GET | `/users/me/merchant` | JWT | Estado de la solicitud |
| PATCH | `/users/me/merchant` | JWT | Edita; cambio crítico regresa a PENDING |
| GET | `/users/merchants/{id}` | público | Ficha pública de comercio VERIFIED |
| GET | `/users/admin/merchants` | admin | Cola de verificación (PENDING) |
| POST | `/users/admin/merchants/{id}/approve` | admin | Aprueba y promueve rol en auth |
| POST | `/users/admin/merchants/{id}/reject` | admin | Rechaza con motivo |

Operacionales: `/actuator/health`, `/actuator/prometheus`, `/v3/api-docs`, `/swagger-ui.html`.