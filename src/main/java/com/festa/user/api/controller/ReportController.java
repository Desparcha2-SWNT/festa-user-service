package com.festa.user.api.controller;

import com.festa.user.api.security.FestaPrincipal;
import com.festa.user.application.service.ModerationService;
import com.festa.user.domain.model.Report;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** RF14: cualquier usuario reporta un evento o a otro usuario. La app llama /api/reports. */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ModerationService moderation;

    public ReportController(ModerationService moderation) {
        this.moderation = moderation;
    }

    public record CreateReportRequest(
            @NotBlank String targetType,
            @NotNull UUID targetId,
            @NotBlank String reason,
            @Size(max = 500) String comment
    ) {
    }

    public record ReportResponse(
            UUID reportId,
            String targetType,
            UUID targetId,
            String reason,
            String comment,
            String status,
            String outcome,
            String action,
            UUID reporterId,
            Instant createdAt,
            Instant resolvedAt
    ) {
        public static ReportResponse of(Report r) {
            return new ReportResponse(r.id(), r.targetType(), r.targetId(), r.reason(), r.comment(), r.status(),
                    r.outcome(), r.action(), r.reporterId(), r.createdAt(), r.resolvedAt());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse create(@AuthenticationPrincipal FestaPrincipal principal, @Valid @RequestBody CreateReportRequest req) {
        return ReportResponse.of(moderation.report(principal.accountId().value(), req.targetType(), req.targetId(), req.reason(), req.comment()));
    }

    @GetMapping("/mine")
    public List<ReportResponse> mine(@AuthenticationPrincipal FestaPrincipal principal) {
        return moderation.myReports(principal.accountId().value()).stream().map(ReportResponse::of).toList();
    }
}
