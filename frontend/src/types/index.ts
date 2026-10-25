export interface AdminStats {
  totalTests: number;
  publishedTests: number;
  draftTests: number;
  totalAttempts: number;
}

export interface TestSeries {
  id: number;
  title: string;
  instructions?: string;
  durationMinutes: number;
  status: 'DRAFT' | 'PUBLISHED';
  accessType: 'FREE' | 'PAID';
  price: number;
  maxAttempts: number;
  maxSolutionViews: number;
  maxPdfDownloads: number;
  sourcePdfPath?: string;
  solutionPdfPath?: string;
  createdAt: string;
}

export interface TestRequest {
  title: string;
  instructions?: string;
  durationMinutes: number;
  status?: 'DRAFT' | 'PUBLISHED';
  accessType: 'FREE' | 'PAID';
  price?: number;
  maxAttempts: number;
  maxSolutionViews: number;
  maxPdfDownloads: number;
  sourcePdfPath?: string;
  solutionPdfPath?: string;
}

export interface ApiFieldError {
  field: string;
  message: string;
}

export interface ApiErrorResponse {
  timestamp?: string;
  status: number;
  message: string;
  code?: string;
  errorCode?: string;
  errors?: ApiFieldError[];
}

export interface ParseResult {
  questionsCreated: number;
  warnings: string[];
}

export interface Question {
  id: number;
  testId: number;
  orderNo: number;
  text: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  correctOption?: 'A' | 'B' | 'C' | 'D' | null;
  marks: number;
  negativeMarks: number;
}

export interface QuestionRequest {
  text: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  correctOption?: 'A' | 'B' | 'C' | 'D' | '' | null;
  marks: number;
  negativeMarks: number;
}
