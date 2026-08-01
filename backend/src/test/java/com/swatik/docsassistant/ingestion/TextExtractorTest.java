package com.swatik.docsassistant.ingestion;

import com.swatik.docsassistant.exception.StorageException;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class TextExtractorTest {

    private final TextExtractor extractor = new TextExtractor();

    @Test
    void extractsTextFromPlainTextFile(@TempDir Path tempDir) throws IOException{
        // Given a .txt file with known content on disk.
        Path file = tempDir.resolve("note.txt");
        Files.writeString(file, "Hello RAG world.\nSecond line.", StandardCharsets.UTF_8);

        // When we extract its text...
        String text = extractor.extract(file.toString());

        // Then the extracted text contains what we wrote
        assertThat(text).contains("Hello RAG world.").contains("Second line.");
    }

    @Test
    void extractsTextFromMarkdownFile(@TempDir Path tempDir) throws IOException{
        // markdown is plain text to Tika; the heading text should survive.
        Path file = tempDir.resolve("doc.md");
        Files.writeString(file, "# Title\n\nSome **bold** body text.", StandardCharsets.UTF_8);

        // When we extract its text...
        String text = extractor.extract(file.toString());

        // Then the extracted text contains what we wrote
        assertThat(text).contains("Title").contains("body text");
    }

    @Test
    void throwsWhenFileMissing(@TempDir Path tempDir) throws IOException{
        // A non-existent path should fail fast with our domain exception.
        Path missing = tempDir.resolve("does_not_exist.txt");

        assertThatThrownBy(() -> extractor.extract(missing.toString()))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("not found");
    }
}
