package com.swatik.docsassistant.service;


import com.swatik.docsassistant.ai.EmbeddingClient;
import com.swatik.docsassistant.ingestion.DocumentChunker;
import com.swatik.docsassistant.ingestion.TextExtractor;
import com.swatik.docsassistant.model.entity.Document;
import com.swatik.docsassistant.repository.ChunkVectorWriter;
import com.swatik.docsassistant.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Orchestrates the ingestion pipeline for a single document
 * extract text -> chunk -> embed each chunk -> store in pgvector
 * <p>
 * Runs asynchronously so the upload HTTP request returns immediately and the embedding work happens in the background.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    // Types we can extract text from.
    private static final Set<String> SUPPORTED_TYPES = Set.of("PDF", "DOCX", "MD", "TXT", "XLSX", "XLS", "CSV");

    // Keep the stored failure reason within DB column
    private static final int MAX_REASON_LENGTH = 1000;

    private final DocumentRepository documentRepository;
    private final ChunkVectorWriter chunkVectorWriter;
    private final TextExtractor textExtractor;
    private final DocumentChunker documentChunker;
    private final EmbeddingClient embeddingClient;


    public IngestionService(DocumentRepository documentRepository, ChunkVectorWriter chunkVectorWriter, TextExtractor textExtractor, DocumentChunker documentChunker, EmbeddingClient embeddingClient) {
        this.documentRepository = documentRepository;
        this.chunkVectorWriter = chunkVectorWriter;
        this.textExtractor = textExtractor;
        this.documentChunker = documentChunker;
        this.embeddingClient = embeddingClient;
    }

    // Starts ingestion once the upload/reindex transaction has commited.
    @Async("ingestionExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDocumentUploaded(DocumentUploadedEvent event) {
        ingest(event.documentId());
    }

    // Ingests the document with the given id.
    // Any failure is caught and recorded on the document as status FAILED so the error is visible to the user.
    public void ingest(UUID documentId) {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null) {
            log.warn("Ingestion skipped: document {} no longer exists", documentId);
            return;
        }

        try {
            // Mark PROCESSING so the UI shows progress while we work
            document.markProcessing();
            documentRepository.save(document);

            // Reject types we can't extract text from yet (image/audio/video)
            if (!SUPPORTED_TYPES.contains(document.getType())) {
                throw new IllegalStateException("Unsupported type for ingestion: " + document.getType());
            }

            // Clear any previous chunks
            chunkVectorWriter.deleteByDocumentId(documentId);

            // Extract raw text, then split into overlapping chunks.
            String text = textExtractor.extract(document.getStoragePath());
            List<String> chunks = documentChunker.chunk(text);

            // Embed each chunk and store it with its vector.
            int ordinal = 0;
            for (String chunk : chunks) {
                float[] embedding = embeddingClient.embed(chunk);
                chunkVectorWriter.insert(UUID.randomUUID(), documentId, ordinal, chunk, embedding, Instant.now());
                ordinal++;
            }

            // Success: mark READY with the final chunk count.
            document.markReady(chunks.size());
            documentRepository.save(document);
            log.info("Ingested document {} -> {} chunks", documentId, chunks.size());
        } catch (Exception e) {
            // On any failure, roll back partial chunks and record the reason.
            log.error("Ingestion failed for the document {}", documentId, e);
            safeDeleteChunks(documentId);
            document.markFailed(truncate(e.getMessage()));
            documentRepository.save(document);
        }
    }

    // Deleting cleanup must never throw and mask original failure
    private void safeDeleteChunks(UUID documentId) {
        try {
            chunkVectorWriter.deleteByDocumentId(documentId);
        } catch (Exception cleanupError) {
            log.warn("Failed to clean up chunks for {}", documentId, cleanupError);
        }
    }

    private String truncate(String message) {
        if (message == null) {
            return "Ingestion failed";
        }

        return message.length() <= MAX_REASON_LENGTH ? message : message.substring(0, MAX_REASON_LENGTH);
    }
}
