package com.festa.user.domain.port.out;

import java.time.Instant;

/**
 * Inyectable para testear transiciones y createdAt/updatedAt de forma determinista.
 */
public interface ClockPort {
    Instant now();
}