package com.festa.user.domain.port.in;

import com.festa.user.application.command.UpdateMerchantCommand;
import com.festa.user.domain.model.MerchantProfile;

public interface UpdateMerchantUseCase {
    MerchantProfile handle(UpdateMerchantCommand command);
}