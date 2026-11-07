package com.festa.user.api.dto.request;

import com.festa.user.domain.vo.Interest;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record ReplaceInterestsRequest(
        @NotNull(message = "interests es obligatorio")
        Set<Interest> interests
) {
}