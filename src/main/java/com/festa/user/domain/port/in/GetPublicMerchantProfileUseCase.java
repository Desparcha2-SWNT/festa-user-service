package com.festa.user.domain.port.in;

import com.festa.user.application.query.PublicMerchantView;
import com.festa.user.domain.vo.AccountId;

public interface GetPublicMerchantProfileUseCase {
    PublicMerchantView handle(AccountId accountId);
}