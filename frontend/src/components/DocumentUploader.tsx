import { useRef, useState } from "react";
import { toast } from "sonner";
import { useUploadDocument } from "../hooks/useDocuments";

const MAX_SIZE_BYTES = 50 * 1024 * 1024; // 50 MB

// Drag and drop file upload component. uploads one file at a time and surfaces success/error toasts.
export function DocumentUploader() {
    const inputRef = useRef<HTMLInputElement>(null);
    const [isDragging, setIsDragging] = useState(false);
    const { mutate: upload, isPending } = useUploadDocument();

    function handleFiles(files: FileList | null) {
        if (!files || files.length === 0) return;
        const file = files[0];
        if (file.size > MAX_SIZE_BYTES) {
            toast.error(`"${file.name}" is larger than 50MB.`);
            return;
        }
        upload(file, {
            onSuccess: (doc) => toast.success(`Uploaded "${doc.filename}".`),
            onError: (error) => toast.error(error.message),
        });
    }

    function handleDrop(event: React.DragEvent<HTMLDivElement>) {
        event.preventDefault();
        setIsDragging(false);
        handleFiles(event.dataTransfer.files);
    }

    function handleDragOver(event: React.DragEvent<HTMLDivElement>) {
        event.preventDefault();
        setIsDragging(true);
    }

    function handleDragLeave(event: React.DragEvent<HTMLDivElement>) {
        event.preventDefault();
        setIsDragging(false);
    }

    function openFilePicker() {
        inputRef.current?.click();
    }

    function handleKeyDown(event: React.KeyboardEvent<HTMLDivElement>) {
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            openFilePicker();
        }
    }

    return (
        <div
            role="button"
            tabIndex={0}
            aria-disabled={isPending}
            onClick={openFilePicker}
            onKeyDown={handleKeyDown}
            onDrop={handleDrop}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            className={`flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed px-6 py-10 text-center transition-colors ${isDragging
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-slate-300 bg-white hover:border-slate-400'
                } ${isPending ? 'pointer-events-none opacity-60' : ''}`} >

            <input
                ref={inputRef}
                type="file"
                className="hidden"
                onChange={(event) => {
                    handleFiles(event.target.files);
                    event.target.value = '';
                }}
            />
            <p className="text-sm font-medium text-slate-700">
                {isPending ? 'Uploading...' : 'Drag & drop a file here, or click to browse'}
            </p>
            <p className="text-xs text-slate-500">PDF, DOCX, MD, TXT, XLSX, CSV... upto 50 MB. </p>
        </div>
    );
}