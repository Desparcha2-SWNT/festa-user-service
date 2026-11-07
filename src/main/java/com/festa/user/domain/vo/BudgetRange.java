package com.festa.user.domain.vo;

/**
 * Rango de presupuesto preferido por el usuario. Lo consume el AI Service
 * para las recomendaciones conversacionales (RF8).
 */
public enum BudgetRange {
    LOW,
    MEDIUM,
    HIGH,
    ANY
}