--Ingestion schema
-- 1. Extra ingestion metadata on documents.
ALTER TABLE documents
    ADD COLUMN chunk_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN failure_reason VARCHAR(1024);

-- 2. pgvector extension (image is pgvector/pgvector:pg16)
CREATE EXTENSION IF NOT EXISTS vector;

-- 3. Chunks: one row per text chunk, with its embedding (nomic-embed-text = 768 dims).
CREATE TABLE chunks (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    ordinal INTEGER NOT NULL,
    content TEXT NOT NULL,
    embedding vector(768),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_chunk_document_ordinal UNIQUE (document_id, ordinal)
);

CREATE INDEX idx_chunks_document_id ON chunks (document_id);