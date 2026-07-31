import type { HealthResponse } from "../types/health";
import { apiGet } from "./client";

// GET /api/health - returns backend service status
export function getHealth(): Promise<HealthResponse> {
  return apiGet<HealthResponse>('/health');
}