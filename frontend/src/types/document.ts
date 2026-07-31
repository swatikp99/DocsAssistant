export type DocumentType =
    | 'PDF'
    | 'DOCX'
    | 'MD'
    | 'TXT'
    | 'XLSX'
    | 'XLS'
    | 'CSV'
    | 'IMAGE'
    | 'AUDIO'
    | 'VIDEO'
    | 'OTHER';

// Ingestion status
export type DocumentStatus = 'UPLOADED' | 'PROCESSING' | 'READY' | 'FAILED';

// Response body for a single document. Mirrors the backend DocumentResponse record.
export interface DocumentResponse {
    id: string;
    filename: string;
    type: DocumentType;
    sizeBytes: number;
    status: DocumentStatus;
    createdAt: string;
}