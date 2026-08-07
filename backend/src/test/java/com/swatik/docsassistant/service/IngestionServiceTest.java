package com.swatik.docsassistant.service;

import com.swatik.docsassistant.ai.EmbeddingClient;
import com.swatik.docsassistant.ingestion.DocumentChunker;
import com.swatik.docsassistant.ingestion.TextExtractor;
import com.swatik.docsassistant.model.entity.Document;
import com.swatik.docsassistant.repository.ChunkVectorWriter;
import com.swatik.docsassistant.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IngestionServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private ChunkVectorWriter chunkVectorWriter;
    @Mock
    private TextExtractor textExtractor;
    @Mock
    private DocumentChunker documentChunker;
    @Mock
    private EmbeddingClient embeddingClient;

    @InjectMocks
    private IngestionService ingestionService;

    private Document newDocument(String type) {
        return new Document(UUID.randomUUID(), "file." + type.toLowerCase(),
                "text/plain", type, 10, "/tmp/file", "UPLOADED", Instant.now());
    }

    @Test
    void ingestsSupportedDocumentReady() {
        Document doc = newDocument("TXT");
        when(documentRepository.findById(doc.getId())).thenReturn(Optional.of(doc));
        when(textExtractor.extract(doc.getStoragePath())).thenReturn("some text");
        when(documentChunker.chunk("some text")).thenReturn(List.of("c1", "c2"));
        when(embeddingClient.embed(any(String.class))).thenReturn(new float[]{0.1f, 0.2f});

        ingestionService.ingest(doc.getId());

        // Both chunks embedded and written with their vectors
        verify(embeddingClient, times(2)).embed(any(String.class));
        verify(chunkVectorWriter, times(2)).insert(any(UUID.class), eq(doc.getId()), anyInt(), any(String.class), any(float[].class), any(Instant.class));

        assertThat(doc.getStatus()).isEqualTo("READY");
        assertThat(doc.getChunkCount()).isEqualTo(2);
        assertThat(doc.getFailureReason()).isNull();
    }

    @Test
    void unsupportedTypeIsMarkedFailed() {
        Document doc = newDocument("IMAGE");
        when(documentRepository.findById(doc.getId())).thenReturn(Optional.of(doc));

        ingestionService.ingest(doc.getId());

        verify(textExtractor, never()).extract(any());
        verify(embeddingClient, never()).embed(any(String.class));
        assertThat(doc.getStatus()).isEqualTo("FAILED");
        assertThat(doc.getFailureReason()).contains("Unsupported type");
    }

    @Test
    void extractionFailureIsMarkedFailed() {
        Document doc = newDocument("PDF");
        when(documentRepository.findById(doc.getId())).thenReturn(Optional.of(doc));
        when(textExtractor.extract(doc.getStoragePath())).thenThrow(new RuntimeException("boom"));

        ingestionService.ingest(doc.getId());

        assertThat(doc.getStatus()).isEqualTo("FAILED");
        assertThat(doc.getFailureReason()).contains("boom");
    }
}
