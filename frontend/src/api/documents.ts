import { apiDelete, apiGet, apiUpload } from './client';
import type { DocumentResponse } from '../types/document';

// GET /api/documents - lists documents, newest first
export function listDocuments(): Promise<DocumentResponse[]> {
    return apiGet<DocumentResponse[]>('/documents');
}

// POST /api/documents (multipart) - uploads a single file under the 'file' field
export function uploadDocument(file: File): Promise<DocumentResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return apiUpload<DocumentResponse>('/documents', formData);
}

// DELETE /api/documents/{id} - removes a document(metadata + file on disk)
export function deleteDocument(id: string): Promise<void> {
    return apiDelete(`/documents/${id}`);
}