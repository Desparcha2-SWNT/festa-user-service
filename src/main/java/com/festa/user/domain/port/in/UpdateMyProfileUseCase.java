package com.festa.user.domain.port.in;

import com.festa.user.application.command.UpdateProfileCommand;
import com.festa.user.domain.model.Profile;

public interface UpdateMyProfileUseCase {
    Profile handle(UpdateProfileCommand command);
}