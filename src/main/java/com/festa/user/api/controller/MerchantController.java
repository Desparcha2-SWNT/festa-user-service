package com.festa.user.api.controller;

import com.festa.user.api.dto.request.SubmitMerchantRequest;
import com.festa.user.api.dto.request.UpdateMerchantRequest;
import com.festa.user.api.dto.response.MerchantResponse;
import com.festa.user.api.dto.response.PublicMerchantResponse;
import com.festa.user.api.mapper.DtoMapper;
import com.festa.user.api.security.FestaPrincipal;
import com.festa.user.application.command.SubmitMerchantCommand;
import com.festa.user.application.command.UpdateMerchantCommand;
import com.festa.user.application.query.PublicMerchantView;
import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.port.in.GetMyMerchantUseCase;
import com.festa.user.domain.port.in.GetPublicMerchantProfileUseCase;
import com.festa.user.domain.port.in.SubmitMerchantUseCase;
import com.festa.user.domain.port.in.UpdateMerchantUseCase;
import com.festa.user.domain.vo.AccountId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class MerchantController {

    private final SubmitMerchantUseCase submitMerchant;
    private final GetMyMerchantUseCase getMyMerchant;
    private final UpdateMerchantUseCase updateMerchant;
    private final GetPublicMerchantProfileUseCase getPublicMerchant;
    private final DtoMapper mapper;

    public MerchantController(
            SubmitMerchantUseCase submitMerchant,
            GetMyMerchantUseCase getMyMerchant,
            UpdateMerchantUseCase updateMerchant,
            GetPublicMerchantProfileUseCase getPublicMerchant,
            DtoMapper mapper
    ) {
        this.submitMerchant = submitMerchant;
        this.getMyMerchant = getMyMerchant;
        this.updateMerchant = updateMerchant;
        this.getPublicMerchant = getPublicMerchant;
        this.mapper = mapper;
    }

    @PostMapping("/users/me/merchant")
    public ResponseEntity<MerchantResponse> submit(
            @AuthenticationPrincipal FestaPrincipal principal,
            @Valid @RequestBody SubmitMerchantRequest req
    ) {
        MerchantProfile created = submitMerchant.handle(new SubmitMerchantCommand(
                principal.accountId(),
                req.businessName(),
                req.category(),
                req.address(),
                req.latitude(),
                req.longitude(),
                req.description()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toMerchantResponse(created));
    }

    @GetMapping("/users/me/merchant")
    public MerchantResponse myMerchant(@AuthenticationPrincipal FestaPrincipal principal) {
        MerchantProfile m = getMyMerchant.handle(principal.accountId());
        return mapper.toMerchantResponse(m);
    }

    @PatchMapping("/users/me/merchant")
    public MerchantResponse update(
            @AuthenticationPrincipal FestaPrincipal principal,
            @Valid @RequestBody UpdateMerchantRequest req
    ) {
        MerchantProfile updated = updateMerchant.handle(new UpdateMerchantCommand(
                principal.accountId(),
                req.businessName(),
                req.category(),
                req.address(),
                req.latitude(),
                req.longitude(),
                req.description()
        ));
        return mapper.toMerchantResponse(updated);
    }

    @GetMapping("/users/merchants/{id}")
    public PublicMerchantResponse publicProfile(@PathVariable UUID id) {
        PublicMerchantView view = getPublicMerchant.handle(AccountId.of(id));
        return mapper.toPublicMerchantResponse(view);
    }
}