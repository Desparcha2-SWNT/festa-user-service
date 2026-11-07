package com.festa.user.infrastructure.persistence.mapper;

import com.festa.user.domain.model.MerchantVerificationEvent;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.MerchantStatus;
import com.festa.user.infrastructure.persistence.entity.MerchantVerificationEventEntity;
import org.springframework.stereotype.Component;

@Component
public class MerchantVerificationEventMapper {

    public MerchantVerificationEventEntity toEntity(MerchantVerificationEvent e) {
        MerchantVerificationEventEntity entity = new MerchantVerificationEventEntity();
        entity.setId(e.id());
        entity.setAccountId(e.accountId().value());
        entity.setFromStatus(e.fromStatus() == null ? null : e.fromStatus().name());
        entity.setToStatus(e.toStatus().name());
        entity.setActorId(e.actorId() == null ? null : e.actorId().value());
        entity.setReason(e.reason());
        entity.setCreatedAt(e.createdAt());
        return entity;
    }

    public MerchantVerificationEvent toDomain(MerchantVerificationEventEntity e) {
        return new MerchantVerificationEvent(
                e.getId(),
                AccountId.of(e.getAccountId()),
                e.getFromStatus() == null ? null : MerchantStatus.valueOf(e.getFromStatus()),
                MerchantStatus.valueOf(e.getToStatus()),
                e.getActorId() == null ? null : AccountId.of(e.getActorId()),
                e.getReason(),
                e.getCreatedAt()
        );
    }
}