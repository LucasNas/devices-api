CREATE TABLE IF NOT EXISTS devices (
    id BIGSERIAL PRIMARY KEY,
    external_id UUID NOT NULL UNIQUE,
    name           VARCHAR(255) NOT NULL,
    brand          VARCHAR(255) NOT NULL,
    state          VARCHAR(20)  NOT NULL,
    creation_time  TIMESTAMPTZ  NOT NULL
);
