package com.festa.user.domain.port.in;

import com.festa.user.application.command.ReplaceInterestsCommand;
import com.festa.user.domain.model.UserInterests;

public interface ReplaceInterestsUseCase {
    UserInterests handle(ReplaceInterestsCommand command);
}