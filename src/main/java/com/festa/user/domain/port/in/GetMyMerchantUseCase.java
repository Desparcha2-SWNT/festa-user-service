package com.festa.user.domain.port.in;

import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.vo.AccountId;

public interface GetMyMerchantUseCase {
    MerchantProfile handle(AccountId accountId);
}