package com.swatik.docsassistant.repository;

import com.swatik.docsassistant.model.entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChunkRepository extends JpaRepository<Chunk, UUID> {

    long countByDocumentId(UUID documentId);

    void deleteByDocumentId(UUID documentId);
}
