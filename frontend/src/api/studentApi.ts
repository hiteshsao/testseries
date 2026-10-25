import {
  StudentTestSummary,
  StudentAttemptResponse,
  StudentAnswerRequest,
  StudentAnswerResponse,
  StudentSubmitResult,
  ApiErrorResponse
} from '../types';

export class ApiError extends Error {
  response: ApiErrorResponse;

  constructor(response: ApiErrorResponse) {
    super(response.message || 'API request failed');
    this.name = 'ApiError';
    this.response = response;
  }
}

// DEMO-ONLY: Default student identifier simulated in prototype
const DEFAULT_DEMO_STUDENT = 'demo-student-1';
const DEMO_STUDENT_HEADER = 'X-Demo-Student-Id';

function getHeaders(studentId?: string, extraHeaders?: Record<string, string>): Record<string, string> {
  return {
    [DEMO_STUDENT_HEADER]: studentId || DEFAULT_DEMO_STUDENT,
    ...(extraHeaders || {}),
  };
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

export const studentApi = {
  /**
   * GET /api/student/tests (PUBLISHED only)
   */
  async getPublishedTests(studentId?: string): Promise<StudentTestSummary[]> {
    const res = await fetch('/api/student/tests', {
      headers: getHeaders(studentId),
    });
    return handleResponse<StudentTestSummary[]>(res);
  },

  /**
   * GET /api/student/tests/{id} (PUBLISHED only)
   */
  async getPublishedTestById(testId: number, studentId?: string): Promise<StudentTestSummary> {
    const res = await fetch(`/api/student/tests/${testId}`, {
      headers: getHeaders(studentId),
    });
    return handleResponse<StudentTestSummary>(res);
  },

  /**
   * POST /api/student/tests/{id}/start
   * Creates attempt, sets ends_at = now + duration (server clock),
   * enforces max_attempts and paid-purchase check; returns questions WITHOUT correct_option, marks only.
   */
  async startTest(testId: number, studentId?: string): Promise<StudentAttemptResponse> {
    const res = await fetch(`/api/student/tests/${testId}/start`, {
      method: 'POST',
      headers: getHeaders(studentId),
    });
    return handleResponse<StudentAttemptResponse>(res);
  },

  /**
   * GET /api/student/tests/{id}/active-attempt
   * Returns the active in-progress attempt or throws 404 ApiError if none exists.
   */
  async getActiveAttempt(testId: number, studentId?: string): Promise<StudentAttemptResponse> {
    const res = await fetch(`/api/student/tests/${testId}/active-attempt`, {
      headers: getHeaders(studentId),
    });
    return handleResponse<StudentAttemptResponse>(res);
  },

  /**
   * PUT /api/student/attempts/{id}/answer
   * Saves selected option and marked_for_review; rejects after ends_at or if submitted.
   */
  async saveAnswer(attemptId: number, data: StudentAnswerRequest, studentId?: string): Promise<StudentAnswerResponse> {
    const res = await fetch(`/api/student/attempts/${attemptId}/answer`, {
      method: 'PUT',
      headers: getHeaders(studentId, { 'Content-Type': 'application/json' }),
      body: JSON.stringify(data),
    });
    return handleResponse<StudentAnswerResponse>(res);
  },

  /**
   * GET /api/student/attempts/{id}
   * Resumes the attempt (answers + remaining seconds).
   */
  async getAttempt(attemptId: number, studentId?: string): Promise<StudentAttemptResponse> {
    const res = await fetch(`/api/student/attempts/${attemptId}`, {
      headers: getHeaders(studentId),
    });
    return handleResponse<StudentAttemptResponse>(res);
  },

  /**
   * POST /api/student/attempts/{id}/submit
   * Submits the attempt, calculates score, and prevents double submission.
   */
  async submitAttempt(attemptId: number, studentId?: string): Promise<StudentSubmitResult> {
    const res = await fetch(`/api/student/attempts/${attemptId}/submit`, {
      method: 'POST',
      headers: getHeaders(studentId),
    });
    return handleResponse<StudentSubmitResult>(res);
  },
};
