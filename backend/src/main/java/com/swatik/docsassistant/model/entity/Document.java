package com.swatik.docsassistant.model.entity;

// Metadata of an uploaded document. Binary content is stored in local file system and stores metdata only in postgres.

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    private UUID id;

    @Column(nullable = false, length = 512)
    private String filename;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "storage_path", nullable = false, length = 1024)
    private String storagePath;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Document() {
        // for JPA
    }

    public Document(UUID id, String filename, String contentType, String type, long sizeBytes, String storagePath, String status, Instant createdAt) {
        this.id = id;
        this.filename = filename;
        this.contentType = contentType;
        this.type = type;
        this.sizeBytes = sizeBytes;
        this.storagePath = storagePath;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getType() {
        return type;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
