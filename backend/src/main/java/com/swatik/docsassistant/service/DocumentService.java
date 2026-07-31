package com.swatik.docsassistant.service;

import com.swatik.docsassistant.exception.BadRequestException;
import com.swatik.docsassistant.exception.NotFoundException;
import com.swatik.docsassistant.model.dto.DocumentResponse;
import com.swatik.docsassistant.model.entity.Document;
import com.swatik.docsassistant.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository repository;
    private final StorageService storageService;

    public DocumentService(DocumentRepository repository, StorageService storageService) {
        this.repository = repository;
        this.storageService = storageService;
    }

    @Transactional
    public DocumentResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required and must not be empty");
        }

        UUID id = UUID.randomUUID();
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed";
        String storagePath = storageService.store(file, id);

        Document doc = new Document(
                id,
                originalName,
                file.getContentType(),
                DocumentType.fromFileName(originalName).name(),
                file.getSize(),
                storagePath,
                "UPLOADED",
                Instant.now());
        return DocumentResponse.from(repository.save(doc));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        Document doc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found: " + id));
        storageService.delete(doc.getStoragePath());
        repository.delete(doc);
    }
}
