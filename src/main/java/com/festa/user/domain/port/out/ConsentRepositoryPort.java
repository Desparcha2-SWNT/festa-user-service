package com.festa.user.domain.port.out;

import com.festa.user.domain.model.Consent;
import com.festa.user.domain.vo.AccountId;

import java.util.List;

/**
 * Se guarda el histórico completo. La consulta "último por tipo" se resuelve
 * en el adaptador con ORDER BY granted_at DESC + DISTINCT ON (type).
 */
public interface ConsentRepositoryPort {
    Consent save(Consent consent);
    List<Consent> findLatestPerType(AccountId accountId);
    void deleteByAccountId(AccountId accountId);
}