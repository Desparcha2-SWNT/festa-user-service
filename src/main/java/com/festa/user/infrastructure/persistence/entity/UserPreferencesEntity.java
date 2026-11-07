package com.festa.user.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_preferences")
public class UserPreferencesEntity {

    @Id
    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "budget_range", nullable = false, length = 16)
    private String budgetRange;

    @Column(name = "max_distance_km")
    private Integer maxDistanceKm;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_preferred_zones",
            joinColumns = @JoinColumn(name = "account_id")
    )
    @Column(name = "zone", nullable = false, length = 64)
    private Set<String> preferredZones = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_preferred_days",
            joinColumns = @JoinColumn(name = "account_id")
    )
    @Column(name = "day_of_week", nullable = false, length = 16)
    private Set<String> preferredDays = new HashSet<>();

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public String getBudgetRange() { return budgetRange; }
    public void setBudgetRange(String budgetRange) { this.budgetRange = budgetRange; }
    public Integer getMaxDistanceKm() { return maxDistanceKm; }
    public void setMaxDistanceKm(Integer maxDistanceKm) { this.maxDistanceKm = maxDistanceKm; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Set<String> getPreferredZones() { return preferredZones; }
    public void setPreferredZones(Set<String> preferredZones) { this.preferredZones = preferredZones; }
    public Set<String> getPreferredDays() { return preferredDays; }
    public void setPreferredDays(Set<String> preferredDays) { this.preferredDays = preferredDays; }
}