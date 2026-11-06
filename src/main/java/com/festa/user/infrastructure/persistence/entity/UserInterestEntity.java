package com.festa.user.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_interests")
@IdClass(UserInterestEntity.PK.class)
public class UserInterestEntity {

    @Id
    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Id
    @Column(name = "interest", nullable = false, updatable = false, length = 32)
    private String interest;

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public String getInterest() { return interest; }
    public void setInterest(String interest) { this.interest = interest; }

    public static class PK implements Serializable {
        private UUID accountId;
        private String interest;

        public PK() {}
        public PK(UUID accountId, String interest) {
            this.accountId = accountId;
            this.interest = interest;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK pk)) return false;
            return Objects.equals(accountId, pk.accountId) && Objects.equals(interest, pk.interest);
        }

        @Override
        public int hashCode() { return Objects.hash(accountId, interest); }
    }
}