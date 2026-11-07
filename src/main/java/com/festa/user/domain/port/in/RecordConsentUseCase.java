package com.festa.user.domain.port.in;

import com.festa.user.application.command.RecordConsentCommand;
import com.festa.user.domain.model.Consent;

public interface RecordConsentUseCase {
    Consent handle(RecordConsentCommand command);
}