package com.festa.user.domain.port.in;

import com.festa.user.domain.model.UserPreferences;
import com.festa.user.domain.vo.AccountId;

public interface GetPreferencesUseCase {
    UserPreferences handle(AccountId accountId);
}