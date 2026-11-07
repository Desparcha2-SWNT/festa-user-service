package com.festa.user.api.controller;

import com.festa.user.api.dto.request.UpdatePreferencesRequest;
import com.festa.user.api.dto.response.PreferencesResponse;
import com.festa.user.api.mapper.DtoMapper;
import com.festa.user.api.security.FestaPrincipal;
import com.festa.user.application.command.UpdatePreferencesCommand;
import com.festa.user.domain.model.UserPreferences;
import com.festa.user.domain.port.in.GetPreferencesUseCase;
import com.festa.user.domain.port.in.UpdatePreferencesUseCase;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/preferences")
public class PreferencesController {

    private final GetPreferencesUseCase getPreferences;
    private final UpdatePreferencesUseCase updatePreferences;
    private final DtoMapper mapper;

    public PreferencesController(
            GetPreferencesUseCase getPreferences,
            UpdatePreferencesUseCase updatePreferences,
            DtoMapper mapper
    ) {
        this.getPreferences = getPreferences;
        this.updatePreferences = updatePreferences;
        this.mapper = mapper;
    }

    @GetMapping
    public PreferencesResponse get(@AuthenticationPrincipal FestaPrincipal principal) {
        UserPreferences pref = getPreferences.handle(principal.accountId());
        return mapper.toPreferencesResponse(pref);
    }

    @PutMapping
    public PreferencesResponse update(
            @AuthenticationPrincipal FestaPrincipal principal,
            @Valid @RequestBody UpdatePreferencesRequest req
    ) {
        UserPreferences updated = updatePreferences.handle(new UpdatePreferencesCommand(
                principal.accountId(),
                req.budgetRange(),
                req.preferredZones(),
                req.preferredDays(),
                req.maxDistanceKm()
        ));
        return mapper.toPreferencesResponse(updated);
    }
}