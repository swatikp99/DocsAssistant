package com.swatik.docsassistant.controller;

import com.swatik.docsassistant.exception.NotFoundException;
import com.swatik.docsassistant.model.dto.DocumentResponse;
import com.swatik.docsassistant.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(DocumentController.class)
public class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    void uploadReturnsCreated() throws Exception {
        UUID id = UUID.randomUUID();
        when(documentService.upload(any())).thenReturn(
                new DocumentResponse(id, "report.pdf", "PDF", 1234, "UPLOADED", 0, null, Instant.now()));

        MockMultipartFile file = new MockMultipartFile("file", "report.pdf", MediaType.APPLICATION_PDF_VALUE, "hello".getBytes());

        mockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("report.pdf"))
                .andExpect(jsonPath("$.type").value("PDF"))
                .andExpect(jsonPath("$.status").value("UPLOADED"));
    }

    @Test
    void listReturnsDocuments() throws Exception {
        when(documentService.list()).thenReturn(List.of(
                new DocumentResponse(UUID.randomUUID(),"a.txt","TXT",10,"READY",3,null,Instant.now())));
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename").value("a.txt"));
    }

    @Test
    void deleteMissingReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new NotFoundException("Document not found: "+id))
                .when(documentService).delete(id);

        mockMvc.perform(delete("/api/documents/{id}",id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

}
