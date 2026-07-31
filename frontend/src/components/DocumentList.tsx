import { toast } from 'sonner';
import { useDeleteDocument, useDocuments } from '../hooks/useDocuments';
import type { DocumentResponse } from '../types/document';
import { formatBytes, formatDateTime } from '../lib/format';

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
    const { mutate: remove, isPending } = useDeleteDocument();

    function handleDelete() {
        if (!window.confirm(`Are you sure you want to delete "${doc.filename}"? This cannot be undone.`)) {
            return;
        }
        remove(doc.id, {
            onSuccess: () => toast.success(`Deleted "${doc.filename}".`),
            onError: (error) => toast.error(error.message),
        });
    }

    return (
        <li className="flex items-center gap-4 px-4 py-3">
            <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium text-slate-800">{doc.filename}</p>
                <p className="truncate text-sm text-slate-500">
                    {formatBytes(doc.sizeBytes)} &middot; {formatDateTime(doc.createdAt)}
                </p>
            </div>

            <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
                {doc.type}
            </span>
            <span className="rounded-full bg-slate-50 px-2 py-0.5 text-xs font-medium text-slate-700">
                {doc.status}
            </span>
            <button
                type="button"
                onClick={handleDelete}
                disabled={isPending}
                className="rounded-md px-2 py-1 text-sm font medium text-red-600 hover:bg-red-50 disabled:opacity-50">
                {isPending ? 'Deleting...' : 'Delete'}
            </button>
        </li>
    );
}