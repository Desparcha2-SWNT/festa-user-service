package com.festa.user.infrastructure.persistence.mapper;

import com.festa.user.domain.model.UserInterests;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.Interest;
import com.festa.user.infrastructure.persistence.entity.UserInterestEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class InterestsMapper {

    public List<UserInterestEntity> toEntities(AccountId accountId, Set<Interest> interests) {
        return interests.stream().map(i -> {
            UserInterestEntity e = new UserInterestEntity();
            e.setAccountId(accountId.value());
            e.setInterest(i.name());
            return e;
        }).toList();
    }

    public UserInterests toDomain(AccountId accountId, List<UserInterestEntity> rows, Instant updatedAt) {
        Set<Interest> values = rows.stream()
                .map(r -> Interest.valueOf(r.getInterest()))
                .collect(Collectors.toUnmodifiableSet());
        return new UserInterests(accountId, values, updatedAt);
    }

    /** Al no tener columna updated_at en user_interests, se toma del perfil. */
    public UUID extractAccountId(List<UserInterestEntity> rows) {
        return rows.isEmpty() ? null : rows.get(0).getAccountId();
    }
}