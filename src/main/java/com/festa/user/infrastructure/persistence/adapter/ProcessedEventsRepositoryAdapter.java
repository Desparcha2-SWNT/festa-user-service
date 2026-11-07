package com.festa.user.infrastructure.persistence.adapter;

import com.festa.user.domain.port.out.ProcessedEventsRepositoryPort;
import com.festa.user.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class ProcessedEventsRepositoryAdapter implements ProcessedEventsRepositoryPort {

    private final ProcessedEventJpaRepository repo;

    public ProcessedEventsRepositoryAdapter(ProcessedEventJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean tryMarkProcessed(UUID eventId, String consumer) {
        return repo.insertIfAbsent(eventId, consumer) > 0;
    }
}