package com.festa.user.domain.port.in;

import com.festa.user.application.command.RejectMerchantCommand;
import com.festa.user.domain.model.MerchantProfile;

public interface RejectMerchantUseCase {
    MerchantProfile handle(RejectMerchantCommand command);
}