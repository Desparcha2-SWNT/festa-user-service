package com.festa.user.api.controller;

import com.festa.user.api.dto.request.RecordConsentRequest;
import com.festa.user.api.dto.response.ConsentResponse;
import com.festa.user.api.mapper.DtoMapper;
import com.festa.user.api.security.FestaPrincipal;
import com.festa.user.application.command.RecordConsentCommand;
import com.festa.user.domain.model.Consent;
import com.festa.user.domain.port.in.ListConsentsUseCase;
import com.festa.user.domain.port.in.RecordConsentUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

@RestController
@RequestMapping("/users/me/consents")
public class ConsentsController {

    private final ListConsentsUseCase listConsents;
    private final RecordConsentUseCase recordConsent;
    private final DtoMapper mapper;

    public ConsentsController(
            ListConsentsUseCase listConsents,
            RecordConsentUseCase recordConsent,
            DtoMapper mapper
    ) {
        this.listConsents = listConsents;
        this.recordConsent = recordConsent;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ConsentResponse> list(@AuthenticationPrincipal FestaPrincipal principal) {
        return listConsents.handle(principal.accountId()).stream()
                .map(mapper::toConsentResponse)
                .toList();
    }

    @PostMapping
    public ConsentResponse record(
            @AuthenticationPrincipal FestaPrincipal principal,
            @Valid @RequestBody RecordConsentRequest req,
            HttpServletRequest http
    ) {
        String ipHash = hashIp(http.getRemoteAddr());
        Consent consent = recordConsent.handle(new RecordConsentCommand(
                principal.accountId(),
                req.type(),
                req.granted(),
                req.version(),
                ipHash
        ));
        return mapper.toConsentResponse(consent);
    }

    private String hashIp(String ip) {
        if (ip == null) return null;
        try {
            byte[] h = MessageDigest.getInstance("SHA-256")
                    .digest(ip.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (Exception ex) {
            return null;
        }
    }
}