CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               aggregate_type VARCHAR(80) NOT NULL,
                               aggregate_id UUID NOT NULL,
                               event_type VARCHAR(120) NOT NULL,

                               payload JSONB NOT NULL,

                               status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                               retry_count INTEGER NOT NULL DEFAULT 0,
                               error_message TEXT NULL,

                               fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               fecha_publicacion TIMESTAMP NULL,

                               CONSTRAINT chk_outbox_events_status
                                   CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE TABLE processed_events (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                  event_id UUID UNIQUE NOT NULL,
                                  event_type VARCHAR(120) NOT NULL,
                                  source_service VARCHAR(80) NOT NULL,

                                  processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_outbox_events_status
    ON outbox_events(status);

CREATE INDEX idx_outbox_events_event_type
    ON outbox_events(event_type);

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events(aggregate_type, aggregate_id);

CREATE INDEX idx_processed_events_event_id
    ON processed_events(event_id);

CREATE INDEX idx_processed_events_event_type
    ON processed_events(event_type);