CREATE TABLE campaign (
    id uuid,
    stamp timestamp,
    event_data json,
    event_type VARCHAR(255)
);
CREATE INDEX ON campaign (id);
