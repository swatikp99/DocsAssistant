package com.swatik.docsassistant.service;

import java.util.UUID;

public record DocumentUploadedEvent(UUID documentId) {
}
