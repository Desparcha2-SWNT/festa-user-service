package com.festa.user.domain.port.in;

import com.festa.user.domain.model.MerchantProfile;

import java.util.List;

public interface ListPendingMerchantsUseCase {
    List<MerchantProfile> handle();
}