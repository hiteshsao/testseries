import { AdminStats, TestSeries, TestRequest, ApiErrorResponse } from '../types';

export class ApiError extends Error {
  response: ApiErrorResponse;

  constructor(response: ApiErrorResponse) {
    super(response.message || 'API request failed');
    this.name = 'ApiError';
    this.response = response;
  }
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let errorData: ApiErrorResponse;
    try {
      errorData = await res.json();
    } catch {
      errorData = {
        status: res.status,
        message: res.statusText || 'An unexpected error occurred',
      };
    }
    throw new ApiError(errorData);
  }

  if (res.status === 204) {
    return {} as T;
  }

  return res.json();
}

export const adminApi = {
  async getStats(): Promise<AdminStats> {
    const res = await fetch('/api/admin/stats');
    return handleResponse<AdminStats>(res);
  },

  async getTests(): Promise<TestSeries[]> {
    const res = await fetch('/api/admin/tests');
    return handleResponse<TestSeries[]>(res);
  },

  async getTestById(id: number): Promise<TestSeries> {
    const res = await fetch(`/api/admin/tests/${id}`);
    return handleResponse<TestSeries>(res);
  },

  async createTest(data: TestRequest): Promise<TestSeries> {
    const res = await fetch('/api/admin/tests', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse<TestSeries>(res);
  },

  async updateTest(id: number, data: TestRequest): Promise<TestSeries> {
    const res = await fetch(`/api/admin/tests/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse<TestSeries>(res);
  },

  async publishTest(id: number): Promise<TestSeries> {
    const res = await fetch(`/api/admin/tests/${id}/publish`, {
      method: 'PATCH',
    });
    return handleResponse<TestSeries>(res);
  },

  async deleteTest(id: number): Promise<void> {
    const res = await fetch(`/api/admin/tests/${id}`, {
      method: 'DELETE',
    });
    return handleResponse<void>(res);
  },

  async uploadPdf(id: number, file: File): Promise<TestSeries> {
    const formData = new FormData();
    formData.append('file', file);
    const res = await fetch(`/api/admin/tests/${id}/upload-pdf`, {
      method: 'POST',
      body: formData,
    });
    return handleResponse<TestSeries>(res);
  },

  // DEMO-ONLY: Debug endpoint to get raw extracted text
  async getRawText(id: number): Promise<{ testId: number; sourcePdfPath: string; rawText: string; pageCount?: number; characterCount?: number }> {
    const res = await fetch(`/api/admin/tests/${id}/raw-text`);
    return handleResponse<{ testId: number; sourcePdfPath: string; rawText: string; pageCount?: number; characterCount?: number }>(res);
  },
};
