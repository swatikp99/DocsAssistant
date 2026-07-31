import { useQuery } from "@tanstack/react-query";
import { getHealth } from "../api/health";

// Polls the backend health endpoint every 10 seconds and returns the status
export function useHealth() {
    return useQuery({
        queryKey: ['health'],
        queryFn: getHealth,
        refetchInterval: 10_000,
        retry: 1,
    });
}