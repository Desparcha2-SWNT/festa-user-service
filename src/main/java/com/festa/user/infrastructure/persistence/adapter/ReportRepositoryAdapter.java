package com.festa.user.infrastructure.persistence.adapter;

import com.festa.user.domain.model.Report;
import com.festa.user.domain.port.out.ReportRepositoryPort;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Reportes con JDBC: tabla simple, sin relaciones, no necesita entidad JPA. */
@Component
public class ReportRepositoryAdapter implements ReportRepositoryPort {

    private static final RowMapper<Report> MAPPER = (rs, i) -> new Report(
            rs.getObject("id", UUID.class),
            rs.getObject("reporter_id", UUID.class),
            rs.getString("target_type"),
            rs.getObject("target_id", UUID.class),
            rs.getString("reason"),
            rs.getString("comment"),
            rs.getString("status"),
            rs.getString("outcome"),
            rs.getString("action"),
            rs.getObject("resolved_by", UUID.class),
            toInstant(rs.getTimestamp("resolved_at")),
            toInstant(rs.getTimestamp("created_at"))
    );

    private final NamedParameterJdbcTemplate jdbc;

    public ReportRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }

    private static Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    @Override
    public boolean insertIfNoOpenDuplicate(Report r) {
        int rows = jdbc.update("""
                INSERT INTO reports (id, reporter_id, target_type, target_id, reason, comment, status, created_at)
                VALUES (:id, :reporter, :type, :target, :reason, :comment, 'OPEN', :createdAt)
                ON CONFLICT (reporter_id, target_type, target_id) WHERE status = 'OPEN' DO NOTHING
                """, new MapSqlParameterSource()
                .addValue("id", r.id())
                .addValue("reporter", r.reporterId())
                .addValue("type", r.targetType())
                .addValue("target", r.targetId())
                .addValue("reason", r.reason())
                .addValue("comment", r.comment())
                .addValue("createdAt", ts(r.createdAt())));
        return rows == 1;
    }

    @Override
    public Optional<Report> findById(UUID id) {
        return jdbc.query("SELECT * FROM reports WHERE id = :id", new MapSqlParameterSource("id", id), MAPPER).stream().findFirst();
    }

    @Override
    public Optional<Report> findByIdForUpdate(UUID id) {
        return jdbc.query("SELECT * FROM reports WHERE id = :id FOR UPDATE", new MapSqlParameterSource("id", id), MAPPER)
                .stream().findFirst();
    }

    @Override
    public void update(Report r) {
        jdbc.update("""
                UPDATE reports SET status = :status, outcome = :outcome, action = :action,
                  resolved_by = :resolvedBy, resolved_at = :resolvedAt
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("id", r.id())
                .addValue("status", r.status())
                .addValue("outcome", r.outcome())
                .addValue("action", r.action())
                .addValue("resolvedBy", r.resolvedBy())
                .addValue("resolvedAt", ts(r.resolvedAt())));
    }

    @Override
    public List<Report> findByStatus(String status, int limit) {
        return jdbc.query("SELECT * FROM reports WHERE status = :status ORDER BY created_at LIMIT :limit",
                new MapSqlParameterSource().addValue("status", status).addValue("limit", limit), MAPPER);
    }

    @Override
    public List<Report> findByReporter(UUID reporterId, int limit) {
        return jdbc.query("SELECT * FROM reports WHERE reporter_id = :reporter ORDER BY created_at DESC LIMIT :limit",
                new MapSqlParameterSource().addValue("reporter", reporterId).addValue("limit", limit), MAPPER);
    }

    @Override
    public void deleteByReporter(UUID reporterId) {
        jdbc.update("DELETE FROM reports WHERE reporter_id = :reporter", new MapSqlParameterSource("reporter", reporterId));
    }
}
