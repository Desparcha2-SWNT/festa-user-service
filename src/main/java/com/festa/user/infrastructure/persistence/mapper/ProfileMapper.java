package com.festa.user.infrastructure.persistence.mapper;

import com.festa.user.domain.model.Profile;
import com.festa.user.domain.port.out.PiiEncryptorPort;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.infrastructure.persistence.entity.ProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    private final PiiEncryptorPort piiEncryptor;

    public ProfileMapper(PiiEncryptorPort piiEncryptor) {
        this.piiEncryptor = piiEncryptor;
    }

    public ProfileEntity toEntity(Profile profile) {
        ProfileEntity e = new ProfileEntity();
        e.setAccountId(profile.accountId().value());
        e.setBio(profile.bio());
        e.setAvatarUrl(profile.avatarUrl());
        e.setPhoneEnc(profile.phone() == null ? null : piiEncryptor.encrypt(profile.phone()));
        e.setCreatedAt(profile.createdAt());
        e.setUpdatedAt(profile.updatedAt());
        return e;
    }

    public Profile toDomain(ProfileEntity e) {
        return new Profile(
                AccountId.of(e.getAccountId()),
                e.getBio(),
                e.getAvatarUrl(),
                e.getPhoneEnc() == null ? null : piiEncryptor.decrypt(e.getPhoneEnc()),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}