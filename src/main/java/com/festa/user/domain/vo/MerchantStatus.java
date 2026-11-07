package com.festa.user.domain.vo;

public enum MerchantStatus {

    PENDING,
    VERIFIED,
    REJECTED;

    public boolean canTransitionTo(MerchantStatus next) {
        if (next == null) return false;
        return switch (this) {
            case PENDING  -> next == VERIFIED || next == REJECTED;
            case VERIFIED -> next == PENDING;
            case REJECTED -> next == PENDING;
        };
    }

    public boolean isPubliclyVisible() {
        return this == VERIFIED;
    }
}
