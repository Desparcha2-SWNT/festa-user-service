package com.festa.user.domain.port.in;

import com.festa.user.application.command.ApproveMerchantCommand;
import com.festa.user.domain.model.MerchantProfile;

public interface ApproveMerchantUseCase {
    MerchantProfile handle(ApproveMerchantCommand command);
}