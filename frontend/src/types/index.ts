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
  problems?: string[];
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

export interface StudentTestSummary {
  id: number;
  title: string;
  instructions?: string;
  durationMinutes: number;
  status: 'PUBLISHED';
  accessType: 'FREE' | 'PAID';
  price: number;
  maxAttempts: number;
  attemptsUsed: number;
  purchased: boolean;
  canAttempt: boolean;
  totalQuestions?: number;
  totalMarks?: number;
  createdAt: string;
}

export interface StudentQuestion {
  id: number;
  orderNo: number;
  text: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  marks: number;
  negativeMarks: number;
}

export interface StudentAnswer {
  questionId: number;
  selectedOption?: string | null;
  markedForReview: boolean;
}

export interface StudentAnswerRequest {
  questionId: number;
  selectedOption?: string | null;
  markedForReview?: boolean;
}

export interface StudentAnswerResponse {
  questionId: number;
  selectedOption?: string | null;
  markedForReview: boolean;
  remainingSeconds: number;
}

export interface StudentAttemptResponse {
  attemptId: number;
  testId: number;
  testTitle: string;
  studentId: string;
  startedAt: string;
  endsAt: string;
  remainingSeconds: number;
  durationMinutes: number;
  status: 'IN_PROGRESS' | 'SUBMITTED';
  totalMarks: number;
  questions: StudentQuestion[];
  answers: StudentAnswer[];
}

export interface StudentSubmitResult {
  attemptId: number;
  testId: number;
  testTitle: string;
  studentId: string;
  startedAt: string;
  submittedAt: string;
  status: 'SUBMITTED';
  totalMarks: number;
  marksObtained: number;
  totalQuestions: number;
  correct: number;
  incorrect: number;
  unanswered: number;
  percentage: number;
}
