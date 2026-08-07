import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { deleteDocument, listDocuments, reindexDocument, uploadDocument } from '../api/documents';
import type { DocumentResponse } from '../types/document';

const DOCUMENT_KEY = ['documents'] as const;

// Statuses that mean ingestionhas finished; polling can stop for these.
const TERMINAL_STATUSES = ['READY','FAILED'] as const;

// How often to repoll the library while a document is still ingesting(ms).
const POLL_INTERVAL_MS = 5000;

// Fetches the document library.
export function useDocuments() {
    return useQuery({
        queryKey: DOCUMENT_KEY,
        queryFn: listDocuments,
        // refetchInterval recevies the latest query so we can decide per tick whether to keep polling.
        refetchInterval: (query) => {
            const docs = query.state.data;
            if(!docs) return false;
            const hasPending = docs.some(
                (doc) => !TERMINAL_STATUSES.includes(doc.status as (typeof TERMINAL_STATUSES[number])),
            );
            return hasPending ? POLL_INTERVAL_MS : false;
        }
    });
}

// Uploads a file, then refetches the document library on success.
export function useUploadDocument() {
    const queryClient = useQueryClient();
    return useMutation<DocumentResponse, Error, File>({
        mutationFn: uploadDocument,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: DOCUMENT_KEY });
        },
    });
}

// Deletes a document, then refetches the document library on success.
export function useDeleteDocument() {
    const queryClient = useQueryClient();
    return useMutation<void, Error, string>({
        mutationFn: deleteDocument,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: DOCUMENT_KEY });
        },
    });
}

// Re-runs ingestion for a document. On success we invalidate the library so the row flips to PROCESSING and the polling loop picks it up.
export function useReindexDocument(){
    const queryClient = useQueryClient();
    return useMutation<DocumentResponse, Error, string>({
        mutationFn: reindexDocument,
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: DOCUMENT_KEY});
        }
    })
}