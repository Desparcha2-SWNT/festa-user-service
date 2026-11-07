package com.festa.user.application.service;

import com.festa.user.application.cache.ProfileCacheKeys;
import com.festa.user.application.cache.ProfileCacheService;
import com.festa.user.application.query.PublicMerchantView;
import com.festa.user.domain.exception.NotFoundException;
import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.port.in.GetPublicMerchantProfileUseCase;
import com.festa.user.domain.port.out.MerchantRepositoryPort;
import com.festa.user.domain.vo.AccountId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicMerchantService implements GetPublicMerchantProfileUseCase {

    private final MerchantRepositoryPort merchants;
    private final ProfileCacheService cache;

    public PublicMerchantService(MerchantRepositoryPort merchants, ProfileCacheService cache) {
        this.merchants = merchants;
        this.cache = cache;
    }

    @Override
    @Transactional(readOnly = true)
    public PublicMerchantView handle(AccountId accountId) {
        String key = ProfileCacheKeys.publicMerchant(accountId);
        PublicMerchantView cached = cache.get(key, PublicMerchantView.class);
        if (cached != null) {
            return cached;
        }

        MerchantProfile merchant = merchants.findByAccountId(accountId)
                .orElseThrow(() -> NotFoundException.of("Comercio", accountId));

        if (!merchant.isPubliclyVisible()) {
            // No exponemos existencia de PENDING/REJECTED/suspendido.
            throw NotFoundException.of("Comercio", accountId);
        }

        PublicMerchantView view = new PublicMerchantView(
                merchant.accountId(),
                merchant.businessName(),
                merchant.category(),
                merchant.address(),
                merchant.latitude(),
                merchant.longitude(),
                merchant.description()
        );
        cache.put(key, view);
        return view;
    }
}