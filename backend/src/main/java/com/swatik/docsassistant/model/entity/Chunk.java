package com.swatik.docsassistant.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A single text chunk extracted from a DOcument, along with its position within the document. The embedding vector
 * column exists in the DB(pgvector) but is not mapped here yet. It is populated and mapped in embedding client
 */

@Entity
@Table(name = "chunks")
public class Chunk {

    @Id
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private int ordinal;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Chunk() {
        // For JPA
    }

    public Chunk(UUID id, UUID documentId, int ordinal, String content, Instant createdAt) {
        this.id = id;
        this.documentId = documentId;
        this.ordinal = ordinal;
        this.content = content;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
