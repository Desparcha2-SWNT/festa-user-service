package com.festa.user.api.controller;

import com.festa.user.api.controller.ReportController.ReportResponse;
import com.festa.user.api.security.FestaPrincipal;
import com.festa.user.application.service.ModerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * RF14: revisión manual del equipo. Protegido por /users/admin/** = ROLE_ADMIN (SecurityConfig).
 */
@RestController
@RequestMapping("/users/admin")
public class AdminModerationController {

    private final ModerationService moderation;

    public AdminModerationController(ModerationService moderation) {
        this.moderation = moderation;
    }

    public record ResolveRequest(@NotBlank String outcome, String action, @Size(max = 500) String reason, Instant suspendedUntil) {
    }

    public record ReasonRequest(@NotBlank @Size(max = 500) String reason, Instant until) {
    }

    @GetMapping("/reports")
    public List<ReportResponse> openReports() {
        return moderation.openReports().stream().map(ReportResponse::of).toList();
    }

    @PostMapping("/reports/{id}/resolve")
    public ReportResponse resolve(@PathVariable UUID id, @AuthenticationPrincipal FestaPrincipal admin, @Valid @RequestBody ResolveRequest req) {
        return ReportResponse.of(moderation.resolve(id, admin.accountId().value(), req.outcome(), req.action(), req.reason(), req.suspendedUntil()));
    }

    @PostMapping("/moderation/events/{eventId}/hide")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void hideEvent(@PathVariable UUID eventId, @AuthenticationPrincipal FestaPrincipal admin, @Valid @RequestBody ReasonRequest req) {
        moderation.hideEvent(eventId, admin.accountId().value(), req.reason());
    }

    @PostMapping("/moderation/events/{eventId}/restore")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void restoreEvent(@PathVariable UUID eventId, @AuthenticationPrincipal FestaPrincipal admin) {
        moderation.restoreEvent(eventId, admin.accountId().value());
    }

    @PostMapping("/moderation/users/{userId}/suspend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void suspendUser(@PathVariable UUID userId, @AuthenticationPrincipal FestaPrincipal admin, @Valid @RequestBody ReasonRequest req) {
        moderation.suspendUser(userId, admin.accountId().value(), req.reason(), req.until());
    }

    @PostMapping("/moderation/users/{userId}/reinstate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void reinstateUser(@PathVariable UUID userId, @AuthenticationPrincipal FestaPrincipal admin) {
        moderation.reinstateUser(userId, admin.accountId().value());
    }
}
