package com.swatik.docsassistant.service;

import com.swatik.docsassistant.exception.StorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Stores uploaded files on local filesystem.
 * Each file is saved with a UUID based name to avoid collisions.
 * The original filename is preserved only as metadata in DB.
 */
@Service
public class StorageService {
    private final Path root;

    public StorageService(@Value("${app.storage.root:./data/uploads}") String root) {
        this.root = Paths.get(root).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage folder: " + root, e);
        }
    }

    // Stores the given file and returns its absolute path on disk.
    public String store(MultipartFile file, UUID id) {
        String extension = extensionOf(file.getOriginalFilename());
        Path target = root.resolve(id + extension);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + file.getOriginalFilename(), e);
        }

        return target.toString();
    }

    public void delete(String storagePath) {
        try {
            Files.delete(Paths.get(storagePath));
        } catch (IOException e) {
            throw new StorageException("Failed to delete file: " + storagePath, e);
        }
    }

    private String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }

}
