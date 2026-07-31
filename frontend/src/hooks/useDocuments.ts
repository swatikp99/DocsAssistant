import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { deleteDocument, listDocuments, uploadDocument } from '../api/documents';
import type { DocumentResponse } from '../types/document';

const DOCUMENT_KEY = ['documents'] as const;

// Fetches the document library.
export function useDocuments() {
    return useQuery({
        queryKey: DOCUMENT_KEY,
        queryFn: listDocuments,
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