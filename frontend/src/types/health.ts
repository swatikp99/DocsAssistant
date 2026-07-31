// Response body for GET /api/health
export interface HealthResponse {
    status: string;
    service: string;
    timestamp: string;
}