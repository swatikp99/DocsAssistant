package com.swatik.docsassistant.ingestion;

import com.swatik.docsassistant.exception.StorageException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Extracts plain text from an uploaded file using Apache Tika
 * Tika auto-detects the file format from its content(not just the extension)
 */
@Service
public class TextExtractor {

    private static final int NO_WRITE_LIMIT = -1;

    // Extracts the full plain text content of the file at the given path.
    public String extract(String storagePath) {
        Path path = Paths.get(storagePath);

        if (!Files.isRegularFile(path)) {
            throw new StorageException("File to extract not found: " + storagePath, null);
        }

        // Pick the right parser for detected content type.
        Parser parser = new AutoDetectParser();

        // Collect text body of the document.
        BodyContentHandler handler = new BodyContentHandler(NO_WRITE_LIMIT);

        // Metadata is populated by Tika.
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        try (InputStream stream = Files.newInputStream(path)) {
            // Tika reads the stream and writes extracted text into the handler.
            parser.parse(stream, handler, metadata, context);
        } catch (IOException | TikaException | SAXException e) {
            throw new StorageException("Failed to extract text from " + storagePath, e);
        }

        return handler.toString();
    }
}
