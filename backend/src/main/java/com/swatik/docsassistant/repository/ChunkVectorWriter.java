package com.swatik.docsassistant.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.StringJoiner;
import java.util.UUID;

@Repository
public class ChunkVectorWriter {

    private final JdbcTemplate jdbcTemplate;

    public ChunkVectorWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Inserts one chunk with its embedding vector
    public void insert(UUID id, UUID documentId, int ordinal, String content, float[] embedding, Instant createdAt) {

        String sql = "INSERT INTO chunks "
                + "(id, document_id, ordinal, content, embedding, created_at) "
                + "VALUES (?,?,?,?,?::vector, ?)";

        jdbcTemplate.update(sql, id, documentId, ordinal, content, toVectorLiteral(embedding), Timestamp.from(createdAt));
    }

    // Removes all chunks for a document(used before re-indexing and on failure so we never leave a half-ingested document behind)
    public void deleteByDocumentId(UUID documentId) {
        jdbcTemplate.update("DELETE FROM chunks WHERE document_id = ?", documentId);
    }

    // Formats a float[] as pgvector's text form, e.g "[0.1,0.2,0.3]"
    private String toVectorLiteral(float[] embedding) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (float value : embedding) {
            joiner.add(Float.toString(value));
        }
        return joiner.toString();
    }
}
