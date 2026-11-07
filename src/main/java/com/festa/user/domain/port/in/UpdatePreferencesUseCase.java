package com.festa.user.domain.port.in;

import com.festa.user.application.command.UpdatePreferencesCommand;
import com.festa.user.domain.model.UserPreferences;

public interface UpdatePreferencesUseCase {
    UserPreferences handle(UpdatePreferencesCommand command);
}