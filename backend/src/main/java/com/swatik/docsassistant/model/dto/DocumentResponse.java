package com.swatik.docsassistant.model.dto;

import com.swatik.docsassistant.model.entity.Document;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String filename,
        String type,
        long sizeBytes,
        String status,
        Instant createdAt) {

    public static DocumentResponse from(Document doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getFilename(),
                doc.getType(),
                doc.getSizeBytes(),
                doc.getStatus(),
                doc.getCreatedAt());
    }
}
