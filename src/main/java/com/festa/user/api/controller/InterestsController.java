package com.festa.user.api.controller;

import com.festa.user.api.dto.request.ReplaceInterestsRequest;
import com.festa.user.api.dto.response.InterestsResponse;
import com.festa.user.api.mapper.DtoMapper;
import com.festa.user.api.security.FestaPrincipal;
import com.festa.user.application.command.ReplaceInterestsCommand;
import com.festa.user.domain.model.UserInterests;
import com.festa.user.domain.port.in.ReplaceInterestsUseCase;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/interests")
public class InterestsController {

    private final ReplaceInterestsUseCase replaceInterests;
    private final DtoMapper mapper;

    public InterestsController(ReplaceInterestsUseCase replaceInterests, DtoMapper mapper) {
        this.replaceInterests = replaceInterests;
        this.mapper = mapper;
    }

    @PutMapping
    public InterestsResponse replace(
            @AuthenticationPrincipal FestaPrincipal principal,
            @Valid @RequestBody ReplaceInterestsRequest req
    ) {
        UserInterests updated = replaceInterests.handle(new ReplaceInterestsCommand(
                principal.accountId(),
                req.interests()
        ));
        return mapper.toInterestsResponse(updated);
    }
}