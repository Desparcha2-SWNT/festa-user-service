package com.festa.user.infrastructure.persistence.mapper;

import com.festa.user.domain.model.Consent;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.ConsentType;
import com.festa.user.infrastructure.persistence.entity.ConsentEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ConsentMapper {

    public ConsentEntity toEntity(Consent c) {
        ConsentEntity e = new ConsentEntity();
        e.setId(UUID.randomUUID());
        e.setAccountId(c.accountId().value());
        e.setType(c.type().name());
        e.setGranted(c.granted());
        e.setVersion(c.version());
        e.setGrantedAt(c.grantedAt());
        e.setIpHash(c.ipHash());
        return e;
    }

    public Consent toDomain(ConsentEntity e) {
        return new Consent(
                AccountId.of(e.getAccountId()),
                ConsentType.valueOf(e.getType()),
                e.isGranted(),
                e.getVersion(),
                e.getGrantedAt(),
                e.getIpHash()
        );
    }
}