package com.festa.user.infrastructure.persistence.repository;

import com.festa.user.infrastructure.persistence.entity.UserPreferencesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserPreferencesJpaRepository extends JpaRepository<UserPreferencesEntity, UUID> {
}