// Thin fetch wrapper for RAG backend. All api calls go through here so base url and error handling live in one place
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8060/api';

// Backend error payload shape
interface ApiErrorBody {
  message?: string;
  error?: string;
}

// Reads the backend's error message if present, else falls back to the status text.
async function toErrorMessage(response: Response): Promise<string> {
  try {
    const body = (await response.json()) as ApiErrorBody;
    if (body.message) return body.message;
    if (body.error) return body.error;
  } catch {
    // Response has no JSON body; fall through to the status text.
  }

  return `Request failed: ${response.status} ${response.statusText}`;
}

export async function apiGet<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      Accept: 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(await toErrorMessage(response));
  }

  return (await response.json()) as T;
}

// POSTs multipart/form-data (e.g. file uploads). Lets the browser set the boundary.
export async function apiUpload<T>(path: string, formData: FormData): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
    },
    body: formData,
  });

  if (!response.ok) {
    throw new Error(await toErrorMessage(response));
  }

  return (await response.json()) as T;
}

// DELETEs a resource. Expects a 2xx(typically 204 No Content) and returns nothing.
export async function apiDelete(path: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'DELETE',
    headers: {
      Accept: 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(await toErrorMessage(response));
  }
}

// POSTs an action with no request body and returns the JSON respnse.
export async function apiPost<T>(path: string): Promise<T>{
  const response = await fetch(`${API_BASE_URL}${path}`,{
    method: 'POST',
    headers: { Accept: 'application/json'},
  });

  if(!response.ok){
    throw new Error(await toErrorMessage(response));
  }

  return (await response.json()) as T;
}