package com.festa.user.domain.model;

import com.festa.user.domain.exception.ConflictException;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.BusinessCategory;
import com.festa.user.domain.vo.MerchantStatus;

import java.time.Instant;
import java.util.Objects;

/**
 * Perfil de comercio/organizador (RF3) con verificación MANUAL para el piloto.
 *
 * Dos dimensiones ortogonales:
 *   - verificationStatus (PENDING | VERIFIED | REJECTED): estado del proceso de revisión.
 *   - suspended (boolean): moderación de auth (moderation.user_suspended / reinstated).
 *
 * Visibilidad pública = VERIFIED && !suspended.
 *
 * Reglas de transición de verificationStatus (ver MerchantStatus):
 *   PENDING   -> VERIFIED | REJECTED
 *   VERIFIED  -> PENDING   (si cambia un campo crítico)
 *   REJECTED  -> PENDING   (reenvío)
 *
 * Campos críticos (un cambio en VERIFIED devuelve a PENDING):
 *   businessName, category, address, latitude, longitude.
 * Campo no crítico: description (no requiere re-verificación).
 */
public record MerchantProfile(
        AccountId accountId,
        String businessName,
        BusinessCategory category,
        String address,
        double latitude,
        double longitude,
        String description,
        MerchantStatus verificationStatus,
        boolean suspended,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {

    public MerchantProfile {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId es obligatorio");
        }
        if (businessName == null || businessName.isBlank()) {
            throw new IllegalArgumentException("businessName es obligatorio");
        }
        if (category == null) {
            throw new IllegalArgumentException("category es obligatoria");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("address es obligatoria");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude fuera de rango");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude fuera de rango");
        }
        if (verificationStatus == null) {
            throw new IllegalArgumentException("verificationStatus es obligatorio");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("createdAt y updatedAt son obligatorios");
        }
    }

    public static MerchantProfile submit(
            AccountId accountId,
            String businessName,
            BusinessCategory category,
            String address,
            double latitude,
            double longitude,
            String description,
            Instant now
    ) {
        return new MerchantProfile(
                accountId, businessName, category, address, latitude, longitude, description,
                MerchantStatus.PENDING, false, null, now, now
        );
    }

    public MerchantProfile approve(Instant now) {
        if (!verificationStatus.canTransitionTo(MerchantStatus.VERIFIED)) {
            throw ConflictException.invalidTransition(
                    verificationStatus.name(), MerchantStatus.VERIFIED.name()
            );
        }
        return with(MerchantStatus.VERIFIED, suspended, null, now);
    }

    public MerchantProfile reject(String reason, Instant now) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("El motivo de rechazo es obligatorio");
        }
        if (!verificationStatus.canTransitionTo(MerchantStatus.REJECTED)) {
            throw ConflictException.invalidTransition(
                    verificationStatus.name(), MerchantStatus.REJECTED.name()
            );
        }
        return with(MerchantStatus.REJECTED, suspended, reason, now);
    }

    public MerchantProfile applyUpdate(
            String newBusinessName,
            BusinessCategory newCategory,
            String newAddress,
            double newLatitude,
            double newLongitude,
            String newDescription,
            Instant now
    ) {
        boolean criticalChanged = isCriticalChange(
                newBusinessName, newCategory, newAddress, newLatitude, newLongitude
        );

        MerchantStatus next = verificationStatus;
        if (criticalChanged && verificationStatus == MerchantStatus.VERIFIED) {
            next = MerchantStatus.PENDING;
        } else if (verificationStatus == MerchantStatus.REJECTED) {
            next = MerchantStatus.PENDING;
        }

        if (next != verificationStatus && !verificationStatus.canTransitionTo(next)) {
            throw ConflictException.invalidTransition(verificationStatus.name(), next.name());
        }

        String nextReason = next == MerchantStatus.PENDING ? null : rejectionReason;

        return new MerchantProfile(
                accountId, newBusinessName, newCategory, newAddress, newLatitude, newLongitude, newDescription,
                next, suspended, nextReason, createdAt, now
        );
    }

    public MerchantProfile markSuspended(boolean value, Instant now) {
        return with(verificationStatus, value, rejectionReason, now);
    }

    public boolean isPubliclyVisible() {
        return verificationStatus.isPubliclyVisible() && !suspended;
    }

    private boolean isCriticalChange(
            String newBusinessName,
            BusinessCategory newCategory,
            String newAddress,
            double newLatitude,
            double newLongitude
    ) {
        return !Objects.equals(this.businessName, newBusinessName)
                || this.category != newCategory
                || !Objects.equals(this.address, newAddress)
                || Double.compare(this.latitude, newLatitude) != 0
                || Double.compare(this.longitude, newLongitude) != 0;
    }

    private MerchantProfile with(
            MerchantStatus newStatus,
            boolean newSuspended,
            String newReason,
            Instant newUpdatedAt
    ) {
        return new MerchantProfile(
                accountId, businessName, category, address, latitude, longitude, description,
                newStatus, newSuspended, newReason, createdAt, newUpdatedAt
        );
    }
}