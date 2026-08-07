import { toast } from 'sonner';
import { useDeleteDocument, useDocuments, useReindexDocument } from '../hooks/useDocuments';
import type { DocumentResponse, DocumentStatus } from '../types/document';
import { formatBytes, formatDateTime } from '../lib/format';

// Tailwind classes per ingestion status, so each state reads at a glance.
const STATUS_STYLES: Record<DocumentStatus, string> = {
    UPLOADED: 'bg-slate-100 text-slate-600',
    PROCESSING: 'bg-amber-50 text-amber-700',
    READY: 'bg-green-50 text-green-700',
    FAILED: 'bg-red-50 text-red-700',
};
// Document library: lists uploaded files with type, size, date, and a delete action.
export function DocumentList() {
    const { data, isPending, isError, error } = useDocuments();

    if (isPending) {
        return <p className="py-8 text-center text-sm text-slate-500">Loading documents...</p>;
    }

    if (isError) {
        return (
            <p className="py-8 text-center text-sm text-red-600">
                Could not load documents: {error.message}
            </p>
        );

    }

    if (data.length === 0) {
        return (
            <p className="py-8 text-center text-sm text-slate-500">
                No documents uploaded yet. Upload one to get started.
            </p>
        );
    }

    return (
        <ul className="divide-y divide-slate-200 rounded-xl border border-slate-200 bg-white">
            {data.map((doc) => (
                <DocumentRow key={doc.id} doc={doc} />
            ))}
        </ul>
    );
}

function DocumentRow({ doc }: { doc: DocumentResponse }) {
    const { mutate: remove, isPending: isDeleting } = useDeleteDocument();
    const { mutate: reindex, isPending: isReindexing } = useReindexDocument();

    // A document is stil working while UPLOADED or PROCEESING.
    const isIngesting = doc.status === 'UPLOADED' || doc.status === 'PROCESSING';

    function handleDelete() {
        if (!window.confirm(`Are you sure you want to delete "${doc.filename}"? This cannot be undone.`)) {
            return;
        }
        remove(doc.id, {
            onSuccess: () => toast.success(`Deleted "${doc.filename}".`),
            onError: (error) => toast.error(error.message),
        });
    }

    function handleReindex() {
        reindex(doc.id, {
            onSuccess: () => toast.success(`Re-indexing "${doc.filename}"...`),
            onError: (error) => toast.error(error.message),
        });
    }

    return (
        <li className="flex items-center gap-4 px-4 py-3">
            <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium text-slate-800">{doc.filename}</p>
                <p className="truncate text-sm text-slate-500">
                    {formatBytes(doc.sizeBytes)} &middot; {formatDateTime(doc.createdAt)}
                    {doc.status === 'READY' && ` . ${doc.chunkCount} chunks`}
                </p>
                {doc.status === 'FAILED' && doc.failureReason && (
                    <p className="mt-0.5 truncate text-xs text-red-600" title={doc.failureReason}>
                        {doc.failureReason}
                    </p>
                )}
            </div>

            <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
                {doc.type}
            </span>
            <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_STYLES[doc.status]}`}>
                {isIngesting ? `${doc.status}...` : doc.status}
            </span>
            {!isIngesting && (
                <button type="button" onClick={handleReindex} disabled={isReindexing} 
                    className="rounded-md px-2 py-1 text-sm font-medium text-blue-600 hover:bg-blue-50 disabled:opacity-50">
                        {isReindexing ? 'Reindexing...':'Reindex'}
                    </button>
            )}
            <button
                type="button"
                onClick={handleDelete}
                disabled={isDeleting}
                className="rounded-md px-2 py-1 text-sm font medium text-red-600 hover:bg-red-50 disabled:opacity-50">
                {isDeleting ? 'Deleting...' : 'Delete'}
            </button>
        </li>
    );
}