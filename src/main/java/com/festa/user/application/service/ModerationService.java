package com.festa.user.application.service;

import com.festa.user.domain.event.outbound.EventHiddenEvent;
import com.festa.user.domain.event.outbound.EventRestoredEvent;
import com.festa.user.domain.event.outbound.ReportCreatedEvent;
import com.festa.user.domain.event.outbound.ReportResolvedEvent;
import com.festa.user.domain.event.outbound.UserReinstatedEvent;
import com.festa.user.domain.event.outbound.UserSuspendedEvent;
import com.festa.user.domain.exception.ConflictException;
import com.festa.user.domain.exception.NotFoundException;
import com.festa.user.domain.exception.ValidationException;
import com.festa.user.domain.model.Report;
import com.festa.user.domain.port.out.ClockPort;
import com.festa.user.domain.port.out.OutboxRepositoryPort;
import com.festa.user.domain.port.out.ReportRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * RF14: reportes y moderación manual (piloto ECI). Cada cambio y su mensaje se guardan en la misma
 * transacción (outbox). Los efectos los aplican los consumidores: auth bloquea el login, event oculta
 * los eventos, notification avisa (ver festa-infra/contracts/README.md § Moderación).
 */
@Service
public class ModerationService {

    private final ReportRepositoryPort reports;
    private final OutboxRepositoryPort outbox;
    private final ClockPort clock;

    public ModerationService(ReportRepositoryPort reports, OutboxRepositoryPort outbox, ClockPort clock) {
        this.reports = reports;
        this.outbox = outbox;
        this.clock = clock;
    }

    @Transactional
    public Report report(UUID reporterId, String targetType, UUID targetId, String reason, String comment) {
        if (!Report.TARGET_TYPES.contains(targetType)) {
            throw new ValidationException(new ValidationException.FieldError("targetType", "debe ser event o user"));
        }
        if (!Report.REASONS.contains(reason)) {
            throw new ValidationException(new ValidationException.FieldError("reason", "motivo no válido"));
        }
        if ("user".equals(targetType) && reporterId.equals(targetId)) {
            throw new ValidationException(new ValidationException.FieldError("targetId", "no puedes reportarte a ti mismo"));
        }
        String cleanComment = comment == null || comment.isBlank() ? null : comment.strip();
        Report report = Report.open(reporterId, targetType, targetId, reason, cleanComment, clock.now());
        if (!reports.insertIfNoOpenDuplicate(report)) {
            throw new ConflictException("REPORT_ALREADY_OPEN", "Ya reportaste esto; el equipo lo está revisando");
        }
        outbox.append(ReportCreatedEvent.of(report.id(), reporterId, targetType, targetId, reason, cleanComment, report.createdAt()));
        return report;
    }

    @Transactional(readOnly = true)
    public List<Report> myReports(UUID reporterId) {
        return reports.findByReporter(reporterId, 100);
    }

    @Transactional(readOnly = true)
    public List<Report> openReports() {
        return reports.findByStatus("OPEN", 200);
    }

    /**
     * Cierra un reporte. Con outcome = action_taken se puede aplicar a la vez la acción: ocultar el
     * evento reportado o suspender al usuario reportado.
     */
    @Transactional
    public Report resolve(UUID reportId, UUID adminId, String outcome, String action, String reason, Instant suspendedUntil) {
        if (!Report.OUTCOMES.contains(outcome)) {
            throw new ValidationException(new ValidationException.FieldError("outcome", "debe ser dismissed o action_taken"));
        }
        String effectiveAction = "action_taken".equals(outcome) ? (action == null ? "none" : action) : "none";
        if (!Report.ACTIONS.contains(effectiveAction)) {
            throw new ValidationException(new ValidationException.FieldError("action", "debe ser hide_event, suspend_user o none"));
        }
        Report report = reports.findByIdForUpdate(reportId).orElseThrow(() -> NotFoundException.of("Reporte", reportId));
        if (!report.isOpen()) {
            throw new ConflictException("REPORT_ALREADY_RESOLVED", "El reporte ya está cerrado");
        }
        if ("hide_event".equals(effectiveAction) && !"event".equals(report.targetType())
                || "suspend_user".equals(effectiveAction) && !"user".equals(report.targetType())) {
            throw new ValidationException(new ValidationException.FieldError("action", "la acción no corresponde al tipo de reporte"));
        }

        Instant now = clock.now();
        Report resolved = report.resolve(outcome, effectiveAction, adminId, now);
        reports.update(resolved);
        outbox.append(ReportResolvedEvent.of(report.id(), report.reporterId(), report.targetType(), report.targetId(), outcome, adminId, now));

        String why = reason == null || reason.isBlank() ? "Reporte por " + report.reason() : reason.strip();
        if ("hide_event".equals(effectiveAction)) {
            outbox.append(EventHiddenEvent.of(report.targetId(), report.id(), why, adminId, now));
        } else if ("suspend_user".equals(effectiveAction)) {
            outbox.append(UserSuspendedEvent.of(report.targetId(), report.id(), why, suspendedUntil, adminId, now));
        }
        return resolved;
    }

    @Transactional
    public void hideEvent(UUID eventId, UUID adminId, String reason) {
        outbox.append(EventHiddenEvent.of(eventId, null, requireReason(reason), adminId, clock.now()));
    }

    @Transactional
    public void restoreEvent(UUID eventId, UUID adminId) {
        outbox.append(EventRestoredEvent.of(eventId, adminId, clock.now()));
    }

    @Transactional
    public void suspendUser(UUID userId, UUID adminId, String reason, Instant until) {
        if (userId.equals(adminId)) {
            throw new ValidationException(new ValidationException.FieldError("userId", "no puedes suspenderte a ti mismo"));
        }
        if (until != null && !until.isAfter(clock.now())) {
            throw new ValidationException(new ValidationException.FieldError("until", "debe ser una fecha futura"));
        }
        outbox.append(UserSuspendedEvent.of(userId, null, requireReason(reason), until, adminId, clock.now()));
    }

    @Transactional
    public void reinstateUser(UUID userId, UUID adminId) {
        outbox.append(UserReinstatedEvent.of(userId, adminId, clock.now()));
    }

    private static String requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ValidationException(new ValidationException.FieldError("reason", "el motivo es obligatorio"));
        }
        return reason.strip();
    }
}
