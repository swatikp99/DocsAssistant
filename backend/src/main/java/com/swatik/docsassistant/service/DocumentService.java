package com.swatik.docsassistant.service;

import com.swatik.docsassistant.exception.BadRequestException;
import com.swatik.docsassistant.exception.NotFoundException;
import com.swatik.docsassistant.model.dto.DocumentResponse;
import com.swatik.docsassistant.model.entity.Document;
import com.swatik.docsassistant.repository.DocumentRepository;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    public DocumentService(DocumentRepository repository, StorageService storageService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.storageService = storageService;
        this.eventPublisher = eventPublisher;
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

        DocumentResponse response = DocumentResponse.from(repository.save(doc));

        // Start ingestion after this transaction commits.
        eventPublisher.publishEvent(new DocumentUploadedEvent(id));

        return response;
    }

    @Transactional(readOnly = true)
    public DocumentResponse get(UUID id){
        Document doc = repository.findById(id).orElseThrow(() -> new NotFoundException("Document not found: "+id));
        return DocumentResponse.from(doc);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional
    public DocumentResponse reindex(UUID id){
        Document doc = repository.findById(id).orElseThrow(() -> new NotFoundException("Document not found: "+id));

        // Reflect PROCESSING immediately in the response, then re-run ingestion after this transaction commits
        doc.markProcessing();
        repository.save(doc);
        eventPublisher.publishEvent(new DocumentUploadedEvent(id));
        return DocumentResponse.from(doc);
    }

    @Transactional
    public void delete(UUID id) {
        Document doc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found: " + id));
        storageService.delete(doc.getStoragePath());

        // Chunks are removed automatically via the FK ON DELETE CASCADE
        repository.delete(doc);
    }
}
