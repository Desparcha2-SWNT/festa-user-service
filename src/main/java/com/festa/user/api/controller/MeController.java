package com.festa.user.api.controller;

import com.festa.user.api.dto.request.UpdateProfileRequest;
import com.festa.user.api.dto.response.ProfileResponse;
import com.festa.user.api.mapper.DtoMapper;
import com.festa.user.api.security.FestaPrincipal;
import com.festa.user.application.command.UpdateProfileCommand;
import com.festa.user.application.query.MyProfileView;
import com.festa.user.domain.model.Profile;
import com.festa.user.domain.port.in.GetMyProfileUseCase;
import com.festa.user.domain.port.in.UpdateMyProfileUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me")
public class MeController {

    private final GetMyProfileUseCase getProfile;
    private final UpdateMyProfileUseCase updateProfile;
    private final DtoMapper mapper;

    public MeController(
            GetMyProfileUseCase getProfile,
            UpdateMyProfileUseCase updateProfile,
            DtoMapper mapper
    ) {
        this.getProfile = getProfile;
        this.updateProfile = updateProfile;
        this.mapper = mapper;
    }

    @GetMapping
    public ProfileResponse me(@AuthenticationPrincipal FestaPrincipal principal) {
        MyProfileView view = getProfile.handle(principal.accountId());
        return mapper.toProfileResponse(view);
    }

    @PatchMapping
    public ResponseEntity<ProfileResponse> update(
            @AuthenticationPrincipal FestaPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest req
    ) {
        Profile updated = updateProfile.handle(new UpdateProfileCommand(
                principal.accountId(),
                req.bio(),
                req.avatarUrl(),
                req.phone()
        ));
        MyProfileView view = getProfile.handle(updated.accountId());
        return ResponseEntity.ok(mapper.toProfileResponse(view));
    }
}