package com.festa.user.application.command;

import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.Interest;

import java.util.Set;

public record ReplaceInterestsCommand(
        AccountId accountId,
        Set<Interest> interests
) {
}