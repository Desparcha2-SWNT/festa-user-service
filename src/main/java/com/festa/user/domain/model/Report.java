package com.festa.user.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * RF14: reporte de un evento o de un usuario. En el piloto lo revisa a mano el equipo (admin).
 * {@code action} es lo que se hizo al resolverlo con outcome = action_taken.
 */
public record Report(
        UUID id,
        UUID reporterId,
        String targetType,
        UUID targetId,
        String reason,
        String comment,
        String status,
        String outcome,
        String action,
        UUID resolvedBy,
        Instant resolvedAt,
        Instant createdAt
) {
    public static final Set<String> TARGET_TYPES = Set.of("event", "user");
    public static final Set<String> REASONS = Set.of("spam", "fraude", "contenido_inapropiado", "informacion_falsa", "acoso", "otro");
    public static final Set<String> OUTCOMES = Set.of("dismissed", "action_taken");
    /** Acciones al resolver con outcome = action_taken. */
    public static final Set<String> ACTIONS = Set.of("hide_event", "suspend_user", "none");

    public static Report open(UUID reporterId, String targetType, UUID targetId, String reason, String comment, Instant now) {
        return new Report(UUID.randomUUID(), reporterId, targetType, targetId, reason, comment, "OPEN", null, null, null, null, now);
    }

    public boolean isOpen() {
        return "OPEN".equals(status);
    }

    public Report resolve(String outcome, String action, UUID admin, Instant now) {
        return new Report(id, reporterId, targetType, targetId, reason, comment, "RESOLVED", outcome, action, admin, now, createdAt);
    }
}
