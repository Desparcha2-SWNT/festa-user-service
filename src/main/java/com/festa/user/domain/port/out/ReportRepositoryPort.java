package com.festa.user.domain.port.out;

import com.festa.user.domain.model.Report;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Persistencia de reportes (RF14). */
public interface ReportRepositoryPort {

    /** Inserta el reporte. Devuelve false si el autor ya tiene uno abierto sobre el mismo objetivo. */
    boolean insertIfNoOpenDuplicate(Report report);

    Optional<Report> findById(UUID id);

    /** Bloquea la fila hasta el fin de la transacción (para resolverlo una sola vez). */
    Optional<Report> findByIdForUpdate(UUID id);

    void update(Report report);

    List<Report> findByStatus(String status, int limit);

    List<Report> findByReporter(UUID reporterId, int limit);

    void deleteByReporter(UUID reporterId);
}
