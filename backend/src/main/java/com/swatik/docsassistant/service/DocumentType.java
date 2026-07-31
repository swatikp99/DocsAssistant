package com.swatik.docsassistant.service;

import java.util.Locale;

public enum DocumentType {
    PDF, DOCX, MD, TXT, XLSX, XLS, CSV, IMAGE, AUDIO, VIDEO, OTHER;

    public static DocumentType fromFileName(String filename) {
        if (filename == null) {
            return OTHER;
        }
        int dot = filename.lastIndexOf('.');
        String ext = dot >= 0 ? filename.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        return switch (ext) {
            case "pdf" -> PDF;
            case "docx", "doc" -> DOCX;
            case "md", "markdown" -> MD;
            case "txt" -> TXT;
            case "xlsx" -> XLSX;
            case "xls" -> XLS;
            case "csv" -> CSV;
            case "png", "jpg", "jpeg", "gif", "bmp", "webp", "tiff" -> IMAGE;
            case "mp3", "wav", "m4a", "flac", "ogg" -> AUDIO;
            case "mp4", "mkv", "mov", "avi", "webm" -> VIDEO;
            default -> OTHER;
        };

    }
}
