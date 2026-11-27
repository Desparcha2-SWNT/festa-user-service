package com.festa.user.domain.port.in;

/**
 * Vuelve a publicar merchant.updated de todos los comercios verificados, para que un consumidor que
 * se perdió su aprobación (por ejemplo, ai-service antes de escuchar merchant.verified) se ponga al día.
 * Usa merchant.updated y no merchant.verified: notification-service no lo consume, así que ningún
 * comercio recibe otra vez el aviso de aprobación.
 */
public interface RepublishVerifiedMerchantsUseCase {
    int republish();
}
