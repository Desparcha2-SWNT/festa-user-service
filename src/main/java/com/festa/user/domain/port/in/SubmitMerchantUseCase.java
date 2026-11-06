package com.festa.user.domain.port.in;

import com.festa.user.application.command.SubmitMerchantCommand;
import com.festa.user.domain.model.MerchantProfile;

public interface SubmitMerchantUseCase {
    MerchantProfile handle(SubmitMerchantCommand command);
}