import { useHealth } from "../hooks/useHealth";

// Shows the live connection status of the RAG backend
export function HealthBadge() {
  const { data, isPending, isError } = useHealth();
  
  const isConnected = !isError && data?.status === 'UP';

  let label: string;
  let dotClass: string;
  let badgeClass: string;

  if (isPending) {
    label = 'Connecting...';
    dotClass = 'bg-yellow-500';
    badgeClass = 'bg-yellow-50 text-yellow-800 ring-yellow-600/20';
  } else if (isConnected) {
    label = 'Backend Connected';
    dotClass = 'bg-green-500';
    badgeClass = 'bg-green-100 text-green-800 ring-green-600/20';
  } else {
    label = 'Backend offline';
    dotClass = 'bg-red-500';
    badgeClass = 'bg-red-100 text-red-800 ring-red-600/20';
  }

    return (
        <span className={`inline-flex items-center gap-2 rounded-full px-3 py-1 text-sm font-medium ring-1 ring-inset ${badgeClass}`}>
            <span className={`h-2.5 w-2.5 rounded-full ${dotClass}`} aria-label="true" />
            {label}
        </span>
    );
}