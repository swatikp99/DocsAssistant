package com.swatik.docsassistant.repository;

import com.swatik.docsassistant.model.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findAllByOrderByCreatedAtDesc();
}
