CREATE TABLE IF NOT EXISTS time_limit_exceed_log (
  id SERIAL PRIMARY KEY,
  class_name VARCHAR(255) NOT NULL,
  method_name VARCHAR(255) NOT NULL,
  execution_time_ms BIGINT NOT NULL,
  exceeded_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS datasource_error_log (
  id SERIAL PRIMARY KEY,
  stack_trace TEXT,
  message VARCHAR(1024),
  method_signature VARCHAR(512),
  occurred_at TIMESTAMP NOT NULL
);
