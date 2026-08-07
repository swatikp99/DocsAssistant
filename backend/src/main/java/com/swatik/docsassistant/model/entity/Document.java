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

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount;

    @Column(name = "failure_reason", length = 1024)
    private String failureReason;

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
        this.chunkCount = 0;
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

    public int getChunkCount() { return chunkCount; }

    public String getFailureReason() { return failureReason; }

    public Instant getCreatedAt() {
        return createdAt;
    }

    //Ingestion started: mark PORCESSING and clear any previous failure.
    public void markProcessing(){
        this.status = "PROCESSING";
        this.failureReason = null;
    }

    //Ingestion succeeded: mark READY and record how many chunks were stored.
    public void markReady(int chunkCount){
        this.status = "READY";
        this.chunkCount = chunkCount;
        this.failureReason = null;
    }

    //Ingestion failed: mark FAILED and reset chunk count, keep a short reason.
    public void markFailed(String reason){
        this.status = "FAILED";
        this.chunkCount = 0;
        this.failureReason = reason;
    }
}
