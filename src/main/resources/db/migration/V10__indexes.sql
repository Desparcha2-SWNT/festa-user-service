-- Un índice adicional por si la tabla de outbox crece en el piloto
CREATE INDEX idx_outbox_published_at ON outbox(published_at) WHERE published_at IS NOT NULL;