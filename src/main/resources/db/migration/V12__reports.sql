-- RF14: reportes de eventos o usuarios, revisados a mano por el equipo (piloto ECI).
CREATE TABLE reports (
    id           UUID PRIMARY KEY,
    reporter_id  UUID NOT NULL,
    target_type  VARCHAR(8) NOT NULL,
    target_id    UUID NOT NULL,
    reason       VARCHAR(32) NOT NULL,
    comment      VARCHAR(500),
    status       VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    outcome      VARCHAR(16),
    action       VARCHAR(16),
    resolved_by  UUID,
    resolved_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_reports_target_type CHECK (target_type IN ('event', 'user')),
    CONSTRAINT chk_reports_reason CHECK (reason IN ('spam', 'fraude', 'contenido_inapropiado', 'informacion_falsa', 'acoso', 'otro')),
    CONSTRAINT chk_reports_status CHECK (status IN ('OPEN', 'RESOLVED')),
    CONSTRAINT chk_reports_outcome CHECK (outcome IS NULL OR outcome IN ('dismissed', 'action_taken'))
);

-- Una persona no puede abrir dos reportes sobre lo mismo mientras el primero siga abierto.
CREATE UNIQUE INDEX uq_reports_open_per_target ON reports (reporter_id, target_type, target_id) WHERE status = 'OPEN';
CREATE INDEX idx_reports_status_created ON reports (status, created_at);
CREATE INDEX idx_reports_reporter ON reports (reporter_id);
