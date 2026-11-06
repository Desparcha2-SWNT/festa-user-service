package com.festa.user.api.controller;

import com.festa.user.api.dto.request.RejectMerchantRequest;
import com.festa.user.api.dto.response.MerchantResponse;
import com.festa.user.api.mapper.DtoMapper;
import com.festa.user.api.security.FestaPrincipal;
import com.festa.user.application.command.ApproveMerchantCommand;
import com.festa.user.application.command.RejectMerchantCommand;
import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.port.in.ApproveMerchantUseCase;
import com.festa.user.domain.port.in.ListPendingMerchantsUseCase;
import com.festa.user.domain.port.in.RejectMerchantUseCase;
import com.festa.user.domain.vo.AccountId;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/admin/merchants")
public class AdminMerchantController {

    private final ListPendingMerchantsUseCase listPending;
    private final ApproveMerchantUseCase approveMerchant;
    private final RejectMerchantUseCase rejectMerchant;
    private final DtoMapper mapper;

    public AdminMerchantController(
            ListPendingMerchantsUseCase listPending,
            ApproveMerchantUseCase approveMerchant,
            RejectMerchantUseCase rejectMerchant,
            DtoMapper mapper
    ) {
        this.listPending = listPending;
        this.approveMerchant = approveMerchant;
        this.rejectMerchant = rejectMerchant;
        this.mapper = mapper;
    }

    @GetMapping
    public List<MerchantResponse> pending() {
        return listPending.handle().stream().map(mapper::toMerchantResponse).toList();
    }

    @PostMapping("/{id}/approve")
    public MerchantResponse approve(
            @PathVariable UUID id,
            @AuthenticationPrincipal FestaPrincipal principal,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        String bearer = stripBearer(authorization);
        MerchantProfile approved = approveMerchant.handle(new ApproveMerchantCommand(
                AccountId.of(id), principal.accountId(), bearer
        ));
        return mapper.toMerchantResponse(approved);
    }

    @PostMapping("/{id}/reject")
    public MerchantResponse reject(
            @PathVariable UUID id,
            @AuthenticationPrincipal FestaPrincipal principal,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody RejectMerchantRequest req
    ) {
        String bearer = stripBearer(authorization);
        MerchantProfile rejected = rejectMerchant.handle(new RejectMerchantCommand(
                AccountId.of(id), req.reason(), principal.accountId(), bearer
        ));
        return mapper.toMerchantResponse(rejected);
    }

    private String stripBearer(String authorization) {
        if (authorization == null) {
            return "";
        }
        return authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length())
                : authorization;
    }
}