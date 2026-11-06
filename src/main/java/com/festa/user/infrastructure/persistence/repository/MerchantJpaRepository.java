package com.festa.user.infrastructure.persistence.repository;

import com.festa.user.infrastructure.persistence.entity.MerchantProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MerchantJpaRepository extends JpaRepository<MerchantProfileEntity, UUID> {

    List<MerchantProfileEntity> findByVerificationStatusOrderByUpdatedAtAsc(String status);
}