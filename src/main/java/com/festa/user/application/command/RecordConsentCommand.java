package com.festa.user.application.command;

import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.ConsentType;

public record RecordConsentCommand(
        AccountId accountId,
        ConsentType type,
        boolean granted,
        String version,
        String ipHash
) {
}