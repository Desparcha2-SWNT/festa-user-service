package com.festa.user.domain.port.in;

import com.festa.user.application.query.MyProfileView;
import com.festa.user.domain.vo.AccountId;

public interface GetMyProfileUseCase {
    MyProfileView handle(AccountId accountId);
}