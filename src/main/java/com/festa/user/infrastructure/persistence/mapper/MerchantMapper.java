package com.festa.user.infrastructure.persistence.mapper;

import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.port.out.PiiEncryptorPort;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.BusinessCategory;
import com.festa.user.domain.vo.MerchantStatus;
import com.festa.user.infrastructure.persistence.entity.MerchantProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {

    private final PiiEncryptorPort piiEncryptor;

    public MerchantMapper(PiiEncryptorPort piiEncryptor) {
        this.piiEncryptor = piiEncryptor;
    }

    public MerchantProfileEntity toEntity(MerchantProfile m) {
        MerchantProfileEntity e = new MerchantProfileEntity();
        e.setAccountId(m.accountId().value());
        e.setBusinessName(m.businessName());
        e.setCategory(m.category().name());
        e.setAddressEnc(piiEncryptor.encrypt(m.address()));
        e.setLatitude(m.latitude());
        e.setLongitude(m.longitude());
        e.setDescription(m.description());
        e.setVerificationStatus(m.verificationStatus().name());
        e.setSuspended(m.suspended());
        e.setRejectionReason(m.rejectionReason());
        e.setCreatedAt(m.createdAt());
        e.setUpdatedAt(m.updatedAt());
        return e;
    }

    public MerchantProfile toDomain(MerchantProfileEntity e) {
        return new MerchantProfile(
                AccountId.of(e.getAccountId()),
                e.getBusinessName(),
                BusinessCategory.valueOf(e.getCategory()),
                piiEncryptor.decrypt(e.getAddressEnc()),
                e.getLatitude(),
                e.getLongitude(),
                e.getDescription(),
                MerchantStatus.valueOf(e.getVerificationStatus()),
                e.isSuspended(),
                e.getRejectionReason(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}