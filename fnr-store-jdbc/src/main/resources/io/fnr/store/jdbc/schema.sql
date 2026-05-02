CREATE TABLE IF NOT EXISTS fnr_tasks (
    ticket_id        VARCHAR(36)  NOT NULL PRIMARY KEY,
    job_name         VARCHAR(255) NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    started_at       TIMESTAMP    NOT NULL,
    completed_at     TIMESTAMP,
    timeout_seconds  BIGINT       NOT NULL,
    result_payload   TEXT,
    param_payload    TEXT,
    error_message    TEXT
);

CREATE INDEX IF NOT EXISTS idx_fnr_tasks_status   ON fnr_tasks(status);
CREATE INDEX IF NOT EXISTS idx_fnr_tasks_job_name ON fnr_tasks(job_name);
