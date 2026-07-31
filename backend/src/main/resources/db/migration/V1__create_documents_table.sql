CREATE TABLE documents
(
    id           UUID PRIMARY KEY,
    filename     VARCHAR(512)  NOT NULL,
    content_type VARCHAR(255),
    type         VARCHAR(32)   NOT NULL,
    size_bytes   BIGINT        NOT NULL,
    storage_path VARCHAR(1024) NOT NULL,
    status       VARCHAR(32)   NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_documents_created_at ON documents (created_at DESC)