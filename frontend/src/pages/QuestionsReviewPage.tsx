import React, { useEffect, useState, useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import { adminApi } from '../api/adminApi';
import { TestSeries, Question, QuestionRequest } from '../types';
import {
  FileQuestion,
  PlusCircle,
  Save,
  Trash2,
  AlertTriangle,
  CheckCircle2,
  ArrowLeft,
  Upload,
  RefreshCw,
  Search,
  Sliders,
  Check,
  X,
  Sparkles,
  HelpCircle
} from 'lucide-react';

interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error' | 'info';
}

interface QuestionFormData {
  text: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  correctOption: 'A' | 'B' | 'C' | 'D' | null;
  marks: number;
  negativeMarks: number;
}

export const QuestionsReviewPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const testId = Number(id);

  const [test, setTest] = useState<TestSeries | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Local editable form state per question keyed by question.id
  const [formDataMap, setFormDataMap] = useState<Record<number, QuestionFormData>>({});
  const [savingMap, setSavingMap] = useState<Record<number, boolean>>({});
  const [savedMap, setSavedMap] = useState<Record<number, boolean>>({});

  // Filter & search state
  const [filterType, setFilterType] = useState<'ALL' | 'MISSING_ANSWER'>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  // Delete modal state
  const [deleteModal, setDeleteModal] = useState<{
    isOpen: boolean;
    question: Question | null;
    isDeleting: boolean;
  }>({
    isOpen: false,
    question: null,
    isDeleting: false,
  });

  // Bulk marks modal state
  const [bulkMarksModal, setBulkMarksModal] = useState<{
    isOpen: boolean;
    marks: number;
    negativeMarks: number;
    isApplying: boolean;
  }>({
    isOpen: false,
    marks: 4,
    negativeMarks: 1,
    isApplying: false,
  });

  // Add Question modal state
  const [addModal, setAddModal] = useState<{
    isOpen: boolean;
    data: QuestionFormData;
    isSubmitting: boolean;
    error: string | null;
  }>({
    isOpen: false,
    data: {
      text: '',
      optionA: '',
      optionB: '',
      optionC: '',
      optionD: '',
      correctOption: null,
      marks: 4,
      negativeMarks: 1,
    },
    isSubmitting: false,
    error: null,
  });

  // Toast notification state
  const [toasts, setToasts] = useState<Toast[]>([]);

  const showToast = (message: string, type: 'success' | 'error' | 'info' = 'success') => {
    const toastId = Math.random().toString(36).substring(2, 9);
    setToasts((prev) => [...prev, { id: toastId, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== toastId));
    }, 3500);
  };

  const removeToast = (toastId: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== toastId));
  };

  // Fetch test details & questions
  const loadData = async () => {
    if (!id || isNaN(testId)) {
      setError('Invalid test ID.');
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const [testData, questionList] = await Promise.all([
        adminApi.getTestById(testId),
        adminApi.getQuestions(testId),
      ]);

      setTest(testData);
      setQuestions(questionList);

      // Initialize form data map
      const initialMap: Record<number, QuestionFormData> = {};
      questionList.forEach((q) => {
        initialMap[q.id] = {
          text: q.text || '',
          optionA: q.optionA || '',
          optionB: q.optionB || '',
          optionC: q.optionC || '',
          optionD: q.optionD || '',
          correctOption: (q.correctOption as 'A' | 'B' | 'C' | 'D') || null,
          marks: Number(q.marks) || 4,
          negativeMarks: Number(q.negativeMarks) || 1,
        };
      });
      setFormDataMap(initialMap);
    } catch (err: any) {
      setError(err?.response?.message || err?.message || 'Failed to load test and questions.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [id, testId]);

  // Handle per-question input change
  const handleFieldChange = (
    questionId: number,
    field: keyof QuestionFormData,
    value: any
  ) => {
    setFormDataMap((prev) => ({
      ...prev,
      [questionId]: {
        ...prev[questionId],
        [field]: value,
      },
    }));
    // Reset saved indicator if field changes
    setSavedMap((prev) => ({ ...prev, [questionId]: false }));
  };

  // Handle Save Question
  const handleSaveQuestion = async (question: Question) => {
    const formData = formDataMap[question.id];
    if (!formData) return;

    // Client-side quick check
    if (!formData.text.trim()) {
      showToast(`Question #${question.orderNo}: Question text is required`, 'error');
      return;
    }
    if (!formData.optionA.trim() || !formData.optionB.trim() || !formData.optionC.trim() || !formData.optionD.trim()) {
      showToast(`Question #${question.orderNo}: All options (A, B, C, D) are required`, 'error');
      return;
    }
    if (formData.marks <= 0) {
      showToast(`Question #${question.orderNo}: Marks must be greater than 0`, 'error');
      return;
    }
    if (formData.negativeMarks < 0) {
      showToast(`Question #${question.orderNo}: Negative marks must be 0 or greater`, 'error');
      return;
    }

    setSavingMap((prev) => ({ ...prev, [question.id]: true }));

    try {
      const payload: QuestionRequest = {
        text: formData.text.trim(),
        optionA: formData.optionA.trim(),
        optionB: formData.optionB.trim(),
        optionC: formData.optionC.trim(),
        optionD: formData.optionD.trim(),
        correctOption: formData.correctOption || null,
        marks: Number(formData.marks),
        negativeMarks: Number(formData.negativeMarks),
      };

      const updated = await adminApi.updateQuestion(question.id, payload);

      // Update question in state
      setQuestions((prev) => prev.map((q) => (q.id === question.id ? updated : q)));
      setSavedMap((prev) => ({ ...prev, [question.id]: true }));
      showToast(`Question #${question.orderNo} saved successfully!`, 'success');
    } catch (err: any) {
      showToast(err?.response?.message || err?.message || 'Failed to save question.', 'error');
    } finally {
      setSavingMap((prev) => ({ ...prev, [question.id]: false }));
    }
  };

  // Handle Delete Question Confirm
  const handleDeleteConfirm = async () => {
    const q = deleteModal.question;
    if (!q) return;

    setDeleteModal((prev) => ({ ...prev, isDeleting: true }));

    try {
      await adminApi.deleteQuestion(q.id);
      showToast(`Question #${q.orderNo} deleted. Questions renumbered without gaps.`, 'success');
      setDeleteModal({ isOpen: false, question: null, isDeleting: false });
      // Reload questions to reflect renumbering
      await loadData();
    } catch (err: any) {
      showToast(err?.response?.message || err?.message || 'Failed to delete question.', 'error');
      setDeleteModal((prev) => ({ ...prev, isDeleting: false }));
    }
  };

  // Handle Apply Marks to All
  const handleApplyBulkMarks = async () => {
    if (questions.length === 0) return;
    if (bulkMarksModal.marks <= 0 || bulkMarksModal.negativeMarks < 0) {
      showToast('Positive marks must be > 0 and negative marks >= 0', 'error');
      return;
    }

    setBulkMarksModal((prev) => ({ ...prev, isApplying: true }));

    try {
      const targetMarks = Number(bulkMarksModal.marks);
      const targetNegative = Number(bulkMarksModal.negativeMarks);

      // Update each question in parallel
      await Promise.all(
        questions.map((q) => {
          const currentForm = formDataMap[q.id] || {
            text: q.text,
            optionA: q.optionA,
            optionB: q.optionB,
            optionC: q.optionC,
            optionD: q.optionD,
            correctOption: q.correctOption,
            marks: targetMarks,
            negativeMarks: targetNegative,
          };
          const payload: QuestionRequest = {
            text: currentForm.text,
            optionA: currentForm.optionA,
            optionB: currentForm.optionB,
            optionC: currentForm.optionC,
            optionD: currentForm.optionD,
            correctOption: currentForm.correctOption || null,
            marks: targetMarks,
            negativeMarks: targetNegative,
          };
          return adminApi.updateQuestion(q.id, payload);
        })
      );

      showToast(`Applied +${targetMarks} / -${targetNegative} marks to all ${questions.length} questions!`, 'success');
      setBulkMarksModal({ isOpen: false, marks: 4, negativeMarks: 1, isApplying: false });
      await loadData();
    } catch (err: any) {
      showToast(err?.response?.message || err?.message || 'Failed to apply marks to all questions.', 'error');
      setBulkMarksModal((prev) => ({ ...prev, isApplying: false }));
    }
  };

  // Handle Add Question Submit
  const handleAddQuestionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const data = addModal.data;

    if (!data.text.trim()) {
      setAddModal((prev) => ({ ...prev, error: 'Question prompt is required.' }));
      return;
    }
    if (!data.optionA.trim() || !data.optionB.trim() || !data.optionC.trim() || !data.optionD.trim()) {
      setAddModal((prev) => ({ ...prev, error: 'All 4 options (A, B, C, D) are required.' }));
      return;
    }

    setAddModal((prev) => ({ ...prev, isSubmitting: true, error: null }));

    try {
      const payload: QuestionRequest = {
        text: data.text.trim(),
        optionA: data.optionA.trim(),
        optionB: data.optionB.trim(),
        optionC: data.optionC.trim(),
        optionD: data.optionD.trim(),
        correctOption: data.correctOption || null,
        marks: Number(data.marks),
        negativeMarks: Number(data.negativeMarks),
      };

      const created = await adminApi.createQuestion(testId, payload);

      showToast(`Question #${created.orderNo} added successfully!`, 'success');
      setAddModal({
        isOpen: false,
        data: {
          text: '',
          optionA: '',
          optionB: '',
          optionC: '',
          optionD: '',
          correctOption: null,
          marks: 4,
          negativeMarks: 1,
        },
        isSubmitting: false,
        error: null,
      });

      await loadData();
    } catch (err: any) {
      setAddModal((prev) => ({
        ...prev,
        isSubmitting: false,
        error: err?.response?.message || err?.message || 'Failed to add question.',
      }));
    }
  };

  // Counters
  const totalQuestions = questions.length;
  const questionsMissingAnswers = useMemo(() => {
    return questions.filter((q) => {
      const current = formDataMap[q.id];
      const correct = current ? current.correctOption : q.correctOption;
      return !correct || correct === null;
    }).length;
  }, [questions, formDataMap]);

  // Filtered questions
  const filteredQuestions = useMemo(() => {
    return questions.filter((q) => {
      const form = formDataMap[q.id] || q;

      // Filter by answer status
      if (filterType === 'MISSING_ANSWER') {
        const correct = form.correctOption;
        if (correct && correct !== null) return false;
      }

      // Search query filter
      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase();
        const textMatch = form.text.toLowerCase().includes(query);
        const optMatch =
          form.optionA.toLowerCase().includes(query) ||
          form.optionB.toLowerCase().includes(query) ||
          form.optionC.toLowerCase().includes(query) ||
          form.optionD.toLowerCase().includes(query);
        const orderMatch = String(q.orderNo).includes(query);
        return textMatch || optMatch || orderMatch;
      }

      return true;
    });
  }, [questions, formDataMap, filterType, searchQuery]);

  if (loading) {
    return (
      <div style={{ padding: '4rem 0', textAlign: 'center', color: 'var(--text-secondary)' }}>
        <RefreshCw size={24} className="spin" style={{ margin: '0 auto 1rem', display: 'block', color: 'var(--teal-primary)' }} />
        <p>Loading question bank...</p>
      </div>
    );
  }

  if (error || !test) {
    return (
      <div>
        <div className="alert-box alert-error">
          <AlertTriangle size={20} style={{ flexShrink: 0 }} />
          <span>{error || 'Test not found.'}</span>
        </div>
        <Link to="/tests" className="btn-secondary" style={{ textDecoration: 'none', display: 'inline-flex' }}>
          <ArrowLeft size={16} /> Back to Tests
        </Link>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1060px', margin: '0 auto', paddingBottom: '5rem' }}>
      {/* Toast Notification Container */}
      <div className="toast-container" id="toast-container">
        {toasts.map((toast) => (
          <div key={toast.id} className="toast-item">
            {toast.type === 'success' && <CheckCircle2 size={18} color="#34d399" />}
            {toast.type === 'error' && <AlertTriangle size={18} color="#f87171" />}
            {toast.type === 'info' && <Sparkles size={18} color="#60a5fa" />}
            <span style={{ flex: 1 }}>{toast.message}</span>
            <button
              onClick={() => removeToast(toast.id)}
              style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', padding: 0 }}
            >
              <X size={14} />
            </button>
          </div>
        ))}
      </div>

      {/* Page Header */}
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.35rem' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
              Test #{test.id}
            </span>
            <span className={`status-pill ${test.status === 'PUBLISHED' ? 'status-published' : 'status-draft'}`}>
              {test.status}
            </span>
          </div>
          <h1 className="page-title">{test.title} — Question Review</h1>
          <p className="page-subtitle">
            Review and edit question prompts, set correct options, and adjust marking schemes before publishing.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <Link to={`/tests/${testId}/upload`} className="btn-secondary" style={{ textDecoration: 'none' }}>
            <Upload size={15} /> Upload PDF
          </Link>
          <Link to="/tests" className="btn-secondary" style={{ textDecoration: 'none' }}>
            <ArrowLeft size={15} /> Back to Tests
          </Link>
        </div>
      </div>

      {/* Header Counters & Stats Bar */}
      <div
        id="question-stats-counter-bar"
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))',
          gap: '1rem',
          marginBottom: '1.75rem',
        }}
      >
        {/* Counter: Total Questions */}
        <div
          id="stat-total-questions"
          style={{
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '1.15rem 1.35rem',
            display: 'flex',
            alignItems: 'center',
            gap: '1rem',
          }}
        >
          <div
            style={{
              width: '44px',
              height: '44px',
              borderRadius: 'var(--radius-sm)',
              backgroundColor: 'var(--teal-glow)',
              color: 'var(--teal-primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <FileQuestion size={22} />
          </div>
          <div>
            <div style={{ fontSize: '0.78rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 700, letterSpacing: '0.05em' }}>
              Total Questions
            </div>
            <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-primary)' }}>
              {totalQuestions}
            </div>
          </div>
        </div>

        {/* Counter: Questions Missing Answers */}
        <div
          id="stat-missing-answers"
          style={{
            backgroundColor: 'var(--bg-card)',
            border: questionsMissingAnswers > 0 ? '1px solid rgba(245, 158, 11, 0.4)' : '1px solid rgba(16, 185, 129, 0.4)',
            borderRadius: 'var(--radius-md)',
            padding: '1.15rem 1.35rem',
            display: 'flex',
            alignItems: 'center',
            gap: '1rem',
          }}
        >
          <div
            style={{
              width: '44px',
              height: '44px',
              borderRadius: 'var(--radius-sm)',
              backgroundColor: questionsMissingAnswers > 0 ? 'rgba(245, 158, 11, 0.15)' : 'rgba(16, 185, 129, 0.15)',
              color: questionsMissingAnswers > 0 ? '#fbbf24' : '#34d399',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            {questionsMissingAnswers > 0 ? <AlertTriangle size={22} /> : <CheckCircle2 size={22} />}
          </div>
          <div>
            <div style={{ fontSize: '0.78rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 700, letterSpacing: '0.05em' }}>
              Missing Answers
            </div>
            <div
              style={{
                fontSize: '1.5rem',
                fontWeight: 800,
                color: questionsMissingAnswers > 0 ? '#fbbf24' : '#34d399',
              }}
            >
              {questionsMissingAnswers}
            </div>
          </div>
        </div>

        {/* Quick Action: Bulk Apply Marks */}
        <div
          style={{
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '1.15rem 1.35rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '0.75rem',
          }}
        >
          <div>
            <div style={{ fontSize: '0.78rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 700, letterSpacing: '0.05em' }}>
              Bulk Marking
            </div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
              Set uniform marks
            </div>
          </div>
          <button
            id="bulk-apply-marks-btn"
            type="button"
            className="btn-sm btn-secondary"
            onClick={() => setBulkMarksModal((prev) => ({ ...prev, isOpen: true }))}
            title="Apply marks to all questions in this test"
          >
            <Sliders size={14} color="var(--teal-primary)" /> Apply to All
          </button>
        </div>

        {/* Primary Action: Add Question */}
        <div
          style={{
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '1.15rem 1.35rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '0.75rem',
          }}
        >
          <div>
            <div style={{ fontSize: '0.78rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 700, letterSpacing: '0.05em' }}>
              Manual Add
            </div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
              Append new question
            </div>
          </div>
          <button
            id="add-question-btn"
            type="button"
            className="btn-sm btn-primary"
            onClick={() => setAddModal((prev) => ({ ...prev, isOpen: true }))}
            title="Add a new question to this test series"
          >
            <PlusCircle size={14} /> Add Question
          </button>
        </div>
      </div>

      {/* Filter and Search Controls */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: '1rem',
          marginBottom: '1.5rem',
          flexWrap: 'wrap',
        }}
      >
        {/* Filter Pills */}
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <button
            type="button"
            className={`btn-sm ${filterType === 'ALL' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setFilterType('ALL')}
          >
            All Questions ({totalQuestions})
          </button>
          <button
            type="button"
            className={`btn-sm ${filterType === 'MISSING_ANSWER' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setFilterType('MISSING_ANSWER')}
          >
            <AlertTriangle size={13} color={filterType === 'MISSING_ANSWER' ? '#fff' : '#f59e0b'} />
            Missing Answers Only ({questionsMissingAnswers})
          </button>
        </div>

        {/* Search Input */}
        <div style={{ position: 'relative', minWidth: '240px' }}>
          <Search size={15} style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
          <input
            id="search-questions-input"
            type="text"
            placeholder="Search questions or options..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="form-input"
            style={{ paddingLeft: '2.3rem', fontSize: '0.85rem', height: '38px' }}
          />
        </div>
      </div>

      {/* Empty State */}
      {filteredQuestions.length === 0 && (
        <div className="form-card" style={{ textAlign: 'center', padding: '3.5rem 1.5rem' }}>
          <HelpCircle size={44} style={{ color: 'var(--text-muted)', margin: '0 auto 1rem', display: 'block' }} />
          <h3 style={{ fontSize: '1.15rem', color: 'var(--text-primary)', marginBottom: '0.4rem' }}>
            {totalQuestions === 0
              ? 'No questions found for this test.'
              : 'No questions match your current filter.'}
          </h3>
          <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
            {totalQuestions === 0
              ? 'Upload a question paper PDF or add questions manually to get started.'
              : 'Try clearing the search query or switching to "All Questions".'}
          </p>
          <div style={{ display: 'inline-flex', gap: '0.75rem' }}>
            {totalQuestions === 0 ? (
              <>
                <Link to={`/tests/${testId}/upload`} className="btn-primary" style={{ textDecoration: 'none' }}>
                  <Upload size={16} /> Upload PDF
                </Link>
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => setAddModal((prev) => ({ ...prev, isOpen: true }))}
                >
                  <PlusCircle size={16} /> Add Question Manually
                </button>
              </>
            ) : (
              <button
                type="button"
                className="btn-secondary"
                onClick={() => {
                  setFilterType('ALL');
                  setSearchQuery('');
                }}
              >
                Reset Filters
              </button>
            )}
          </div>
        </div>
      )}

      {/* Questions List — One Card Per Question */}
      <div id="questions-list">
        {filteredQuestions.map((question) => {
          const form = formDataMap[question.id] || {
            text: question.text,
            optionA: question.optionA,
            optionB: question.optionB,
            optionC: question.optionC,
            optionD: question.optionD,
            correctOption: question.correctOption,
            marks: Number(question.marks),
            negativeMarks: Number(question.negativeMarks),
          };

          const isMissingAnswer = !form.correctOption || form.correctOption === null;
          const isSaving = savingMap[question.id] || false;
          const isSaved = savedMap[question.id] || false;

          return (
            <div
              key={question.id}
              id={`question-card-${question.id}`}
              className={`question-card ${isMissingAnswer ? 'missing-answer' : 'has-answer'}`}
            >
              {/* Question Card Header */}
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  marginBottom: '1.25rem',
                  paddingBottom: '0.85rem',
                  borderBottom: '1px solid var(--border-subtle)',
                  flexWrap: 'wrap',
                  gap: '0.75rem',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <span
                    style={{
                      backgroundColor: 'rgba(20, 184, 166, 0.15)',
                      color: 'var(--teal-hover)',
                      fontWeight: 800,
                      fontSize: '0.95rem',
                      padding: '0.25rem 0.75rem',
                      borderRadius: 'var(--radius-sm)',
                      letterSpacing: '0.02em',
                    }}
                  >
                    Question #{question.orderNo}
                  </span>

                  {/* Amber Badge on Questions with No Correct Answer */}
                  {isMissingAnswer ? (
                    <span id={`badge-missing-answer-${question.id}`} className="badge-amber">
                      <AlertTriangle size={12} /> No Correct Answer
                    </span>
                  ) : (
                    <span id={`badge-correct-answer-${question.id}`} className="badge-green">
                      <CheckCircle2 size={12} /> Correct: Option {form.correctOption}
                    </span>
                  )}
                </div>

                {/* Per-Question Actions (Save & Delete) */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                  <button
                    id={`save-question-btn-${question.id}`}
                    type="button"
                    className="btn-sm btn-primary"
                    disabled={isSaving}
                    onClick={() => handleSaveQuestion(question)}
                    title="Save this question"
                  >
                    {isSaving ? (
                      <>
                        <RefreshCw size={13} className="spin" /> Saving...
                      </>
                    ) : isSaved ? (
                      <>
                        <Check size={13} /> Saved
                      </>
                    ) : (
                      <>
                        <Save size={13} /> Save
                      </>
                    )}
                  </button>

                  <button
                    id={`delete-question-btn-${question.id}`}
                    type="button"
                    className="btn-sm btn-action-ghost"
                    onClick={() => setDeleteModal({ isOpen: true, question, isDeleting: false })}
                    title="Delete question with confirmation"
                    style={{ color: '#f87171' }}
                  >
                    <Trash2 size={13} />
                  </button>
                </div>
              </div>

              {/* Editable Question Text Field */}
              <div className="form-group" style={{ marginBottom: '1.25rem' }}>
                <label className="form-label" htmlFor={`question-text-${question.id}`}>
                  Question Prompt <span className="required-star">*</span>
                </label>
                <textarea
                  id={`question-text-${question.id}`}
                  rows={3}
                  className="form-input"
                  style={{ resize: 'vertical', minHeight: '75px', lineHeight: '1.5' }}
                  value={form.text}
                  onChange={(e) => handleFieldChange(question.id, 'text', e.target.value)}
                  placeholder="Enter question text..."
                />
              </div>

              {/* 4 Option Inputs with Correct-Answer Radios */}
              <div style={{ marginBottom: '1.5rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                  <label className="form-label" style={{ margin: 0 }}>
                    Options & Correct Answer <span className="required-star">*</span>
                  </label>
                  <span style={{ fontSize: '0.76rem', color: 'var(--text-muted)' }}>
                    Select the radio button next to the correct option
                  </span>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '0.65rem' }}>
                  {(['A', 'B', 'C', 'D'] as const).map((optKey) => {
                    const fieldName = `option${optKey}` as keyof QuestionFormData;
                    const isSelected = form.correctOption === optKey;

                    return (
                      <div
                        key={optKey}
                        className={`option-row ${isSelected ? 'is-correct' : ''}`}
                      >
                        {/* Radio selection for correct option */}
                        <label
                          htmlFor={`radio-${question.id}-${optKey}`}
                          style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            cursor: 'pointer',
                            userSelect: 'none',
                          }}
                          title={`Mark Option ${optKey} as correct answer`}
                        >
                          <input
                            type="radio"
                            id={`radio-${question.id}-${optKey}`}
                            name={`correct-option-${question.id}`}
                            checked={isSelected}
                            onChange={() => handleFieldChange(question.id, 'correctOption', optKey)}
                            style={{
                              accentColor: 'var(--teal-primary)',
                              width: '18px',
                              height: '18px',
                              cursor: 'pointer',
                            }}
                          />
                          <span
                            style={{
                              marginLeft: '0.5rem',
                              fontWeight: 700,
                              fontSize: '0.85rem',
                              color: isSelected ? '#34d399' : 'var(--text-secondary)',
                              width: '18px',
                            }}
                          >
                            {optKey}.
                          </span>
                        </label>

                        {/* Option text input */}
                        <input
                          id={`option-${optKey.toLowerCase()}-${question.id}`}
                          type="text"
                          className="form-input"
                          style={{
                            flex: 1,
                            backgroundColor: 'transparent',
                            border: 'none',
                            padding: '0.35rem 0.5rem',
                            color: 'var(--text-primary)',
                          }}
                          value={String(form[fieldName] ?? '')}
                          onChange={(e) => handleFieldChange(question.id, fieldName, e.target.value)}
                          placeholder={`Enter text for Option ${optKey}...`}
                        />

                        {isSelected && (
                          <span
                            style={{
                              fontSize: '0.72rem',
                              color: '#34d399',
                              fontWeight: 700,
                              padding: '2px 8px',
                              borderRadius: '4px',
                              backgroundColor: 'rgba(16, 185, 129, 0.15)',
                              whiteSpace: 'nowrap',
                            }}
                          >
                            Correct
                          </span>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Marks and Negative Marks Row */}
              <div
                style={{
                  display: 'flex',
                  gap: '1.25rem',
                  alignItems: 'center',
                  paddingTop: '0.75rem',
                  borderTop: '1px solid rgba(255, 255, 255, 0.05)',
                  flexWrap: 'wrap',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <label htmlFor={`marks-${question.id}`} style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
                    Marks (+):
                  </label>
                  <input
                    id={`marks-${question.id}`}
                    type="number"
                    step="0.25"
                    min="0.01"
                    className="form-input"
                    style={{ width: '90px', padding: '0.4rem 0.6rem', textAlign: 'center' }}
                    value={form.marks}
                    onChange={(e) => handleFieldChange(question.id, 'marks', e.target.value === '' ? '' : Number(e.target.value))}
                  />
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <label htmlFor={`negative-marks-${question.id}`} style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
                    Negative Marks (-):
                  </label>
                  <input
                    id={`negative-marks-${question.id}`}
                    type="number"
                    step="0.25"
                    min="0"
                    className="form-input"
                    style={{ width: '90px', padding: '0.4rem 0.6rem', textAlign: 'center' }}
                    value={form.negativeMarks}
                    onChange={(e) => handleFieldChange(question.id, 'negativeMarks', e.target.value === '' ? '' : Number(e.target.value))}
                  />
                </div>

                <div style={{ marginLeft: 'auto', fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                  Question ID: #{question.id}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Bottom Sticky Action Bar */}
      {questions.length > 0 && (
        <div
          style={{
            position: 'sticky',
            bottom: '1rem',
            backgroundColor: 'rgba(13, 21, 39, 0.95)',
            backdropFilter: 'blur(12px)',
            border: '1px solid var(--border-active)',
            borderRadius: 'var(--radius-lg)',
            padding: '1rem 1.5rem',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            boxShadow: '0 8px 30px rgba(0, 0, 0, 0.45), 0 0 15px var(--teal-glow)',
            flexWrap: 'wrap',
            gap: '1rem',
            zIndex: 40,
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
            <span style={{ fontSize: '0.88rem', color: 'var(--text-primary)', fontWeight: 600 }}>
              {questions.length} Questions Reviewed
            </span>
            {questionsMissingAnswers > 0 ? (
              <span className="badge-amber" style={{ fontSize: '0.78rem' }}>
                <AlertTriangle size={13} /> {questionsMissingAnswers} questions still need answers
              </span>
            ) : (
              <span className="badge-green" style={{ fontSize: '0.78rem' }}>
                <CheckCircle2 size={13} /> All questions have correct answers assigned
              </span>
            )}
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <button
              type="button"
              className="btn-secondary"
              onClick={() => setBulkMarksModal((prev) => ({ ...prev, isOpen: true }))}
            >
              <Sliders size={14} /> Bulk Marks
            </button>
            <button
              type="button"
              className="btn-primary"
              onClick={() => setAddModal((prev) => ({ ...prev, isOpen: true }))}
            >
              <PlusCircle size={15} /> Add Question
            </button>
            <Link to="/tests" className="btn-secondary" style={{ textDecoration: 'none' }}>
              Done / Back to Tests
            </Link>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deleteModal.isOpen && deleteModal.question && (
        <div
          id="delete-confirmation-modal"
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.75)',
            backdropFilter: 'blur(4px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '1rem',
          }}
        >
          <div
            className="form-card"
            style={{
              maxWidth: '480px',
              width: '100%',
              border: '1px solid rgba(244, 63, 94, 0.35)',
              boxShadow: '0 10px 40px rgba(0, 0, 0, 0.5)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
              <div
                style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: '50%',
                  backgroundColor: 'rgba(244, 63, 94, 0.15)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#f87171',
                }}
              >
                <Trash2 size={20} />
              </div>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
                Delete Question #{deleteModal.question.orderNo}?
              </h3>
            </div>

            <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: '1.5', marginBottom: '1.25rem' }}>
              Are you sure you want to delete this question?
              <br />
              <strong>All subsequent questions will be automatically re-numbered</strong> without gaps.
            </p>

            <div
              style={{
                backgroundColor: 'rgba(255, 255, 255, 0.03)',
                padding: '0.75rem 1rem',
                borderRadius: 'var(--radius-sm)',
                fontSize: '0.85rem',
                color: 'var(--text-muted)',
                marginBottom: '1.5rem',
                fontStyle: 'italic',
                maxHeight: '80px',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
              }}
            >
              "{deleteModal.question.text}"
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button
                type="button"
                className="btn-secondary"
                disabled={deleteModal.isDeleting}
                onClick={() => setDeleteModal({ isOpen: false, question: null, isDeleting: false })}
              >
                Cancel
              </button>
              <button
                id="confirm-delete-btn"
                type="button"
                className="btn-primary"
                style={{ backgroundColor: '#e11d48', borderColor: '#e11d48' }}
                disabled={deleteModal.isDeleting}
                onClick={handleDeleteConfirm}
              >
                {deleteModal.isDeleting ? (
                  <>
                    <RefreshCw size={14} className="spin" /> Deleting...
                  </>
                ) : (
                  <>
                    <Trash2 size={14} /> Delete & Renumber
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Bulk Apply Marks Modal */}
      {bulkMarksModal.isOpen && (
        <div
          id="bulk-marks-modal"
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.75)',
            backdropFilter: 'blur(4px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '1rem',
          }}
        >
          <div className="form-card" style={{ maxWidth: '440px', width: '100%' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
              <div
                style={{
                  width: '40px',
                  height: '40px',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'var(--teal-glow)',
                  color: 'var(--teal-primary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Sliders size={20} />
              </div>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
                Apply Marks to All
              </h3>
            </div>

            <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', marginBottom: '1.25rem', lineHeight: '1.5' }}>
              Set standard positive and negative marking scheme for all <strong>{questions.length}</strong> questions in this test series.
            </p>

            <div className="form-group">
              <label className="form-label" htmlFor="bulk-marks-input">
                Marks for Correct Answer (+)
              </label>
              <input
                id="bulk-marks-input"
                type="number"
                step="0.5"
                min="0.01"
                className="form-input"
                value={bulkMarksModal.marks}
                onChange={(e) =>
                  setBulkMarksModal((prev) => ({
                    ...prev,
                    marks: e.target.value === '' ? 0 : Number(e.target.value),
                  }))
                }
              />
            </div>

            <div className="form-group" style={{ marginBottom: '1.5rem' }}>
              <label className="form-label" htmlFor="bulk-negative-marks-input">
                Negative Marks for Incorrect Answer (-)
              </label>
              <input
                id="bulk-negative-marks-input"
                type="number"
                step="0.25"
                min="0"
                className="form-input"
                value={bulkMarksModal.negativeMarks}
                onChange={(e) =>
                  setBulkMarksModal((prev) => ({
                    ...prev,
                    negativeMarks: e.target.value === '' ? 0 : Number(e.target.value),
                  }))
                }
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button
                type="button"
                className="btn-secondary"
                disabled={bulkMarksModal.isApplying}
                onClick={() => setBulkMarksModal((prev) => ({ ...prev, isOpen: false }))}
              >
                Cancel
              </button>
              <button
                id="confirm-bulk-marks-btn"
                type="button"
                className="btn-primary"
                disabled={bulkMarksModal.isApplying}
                onClick={handleApplyBulkMarks}
              >
                {bulkMarksModal.isApplying ? (
                  <>
                    <RefreshCw size={14} className="spin" /> Applying...
                  </>
                ) : (
                  <>
                    <Check size={14} /> Apply to All Questions
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Add Question Modal */}
      {addModal.isOpen && (
        <div
          id="add-question-modal"
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.75)',
            backdropFilter: 'blur(4px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '1rem',
          }}
        >
          <div
            className="form-card"
            style={{
              maxWidth: '680px',
              width: '100%',
              maxHeight: '90vh',
              overflowY: 'auto',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div
                  style={{
                    width: '38px',
                    height: '38px',
                    borderRadius: 'var(--radius-sm)',
                    backgroundColor: 'var(--teal-glow)',
                    color: 'var(--teal-primary)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <PlusCircle size={20} />
                </div>
                <h3 style={{ fontSize: '1.2rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
                  Add New Question
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setAddModal((prev) => ({ ...prev, isOpen: false }))}
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            {addModal.error && (
              <div className="alert-box alert-error" style={{ marginBottom: '1rem', padding: '0.75rem 1rem' }}>
                <AlertTriangle size={16} style={{ flexShrink: 0 }} />
                <span>{addModal.error}</span>
              </div>
            )}

            <form onSubmit={handleAddQuestionSubmit}>
              <div className="form-group">
                <label className="form-label" htmlFor="new-question-text">
                  Question Prompt <span className="required-star">*</span>
                </label>
                <textarea
                  id="new-question-text"
                  rows={3}
                  className="form-input"
                  required
                  placeholder="Enter question prompt..."
                  value={addModal.data.text}
                  onChange={(e) =>
                    setAddModal((prev) => ({
                      ...prev,
                      data: { ...prev.data, text: e.target.value },
                    }))
                  }
                />
              </div>

              {(['A', 'B', 'C', 'D'] as const).map((opt) => (
                <div key={opt} className="form-group" style={{ marginBottom: '0.75rem' }}>
                  <label className="form-label" htmlFor={`new-opt-${opt.toLowerCase()}`}>
                    Option {opt} <span className="required-star">*</span>
                  </label>
                  <input
                    id={`new-opt-${opt.toLowerCase()}`}
                    type="text"
                    className="form-input"
                    required
                    placeholder={`Enter text for Option ${opt}...`}
                    value={addModal.data[`option${opt}`]}
                    onChange={(e) =>
                      setAddModal((prev) => ({
                        ...prev,
                        data: { ...prev.data, [`option${opt}`]: e.target.value },
                      }))
                    }
                  />
                </div>
              ))}

              <div className="form-group" style={{ marginTop: '1rem' }}>
                <label className="form-label">
                  Correct Answer (Optional)
                </label>
                <div style={{ display: 'flex', gap: '1.25rem', marginTop: '0.35rem' }}>
                  {(['A', 'B', 'C', 'D'] as const).map((opt) => (
                    <label key={opt} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem', cursor: 'pointer' }}>
                      <input
                        type="radio"
                        name="new-correct-option"
                        checked={addModal.data.correctOption === opt}
                        onChange={() =>
                          setAddModal((prev) => ({
                            ...prev,
                            data: { ...prev.data, correctOption: opt },
                          }))
                        }
                        style={{ accentColor: 'var(--teal-primary)' }}
                      />
                      <span>Option {opt}</span>
                    </label>
                  ))}
                  <button
                    type="button"
                    className="btn-sm btn-action-ghost"
                    onClick={() =>
                      setAddModal((prev) => ({
                        ...prev,
                        data: { ...prev.data, correctOption: null },
                      }))
                    }
                  >
                    Clear selection
                  </button>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label" htmlFor="new-marks">
                    Marks (+) <span className="required-star">*</span>
                  </label>
                  <input
                    id="new-marks"
                    type="number"
                    step="0.5"
                    min="0.01"
                    className="form-input"
                    required
                    value={addModal.data.marks}
                    onChange={(e) =>
                      setAddModal((prev) => ({
                        ...prev,
                        data: { ...prev.data, marks: e.target.value === '' ? 0 : Number(e.target.value) },
                      }))
                    }
                  />
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                  <label className="form-label" htmlFor="new-negative-marks">
                    Negative Marks (-) <span className="required-star">*</span>
                  </label>
                  <input
                    id="new-negative-marks"
                    type="number"
                    step="0.25"
                    min="0"
                    className="form-input"
                    required
                    value={addModal.data.negativeMarks}
                    onChange={(e) =>
                      setAddModal((prev) => ({
                        ...prev,
                        data: { ...prev.data, negativeMarks: e.target.value === '' ? 0 : Number(e.target.value) },
                      }))
                    }
                  />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button
                  type="button"
                  className="btn-secondary"
                  disabled={addModal.isSubmitting}
                  onClick={() => setAddModal((prev) => ({ ...prev, isOpen: false }))}
                >
                  Cancel
                </button>
                <button
                  id="submit-new-question-btn"
                  type="submit"
                  className="btn-primary"
                  disabled={addModal.isSubmitting}
                >
                  {addModal.isSubmitting ? (
                    <>
                      <RefreshCw size={14} className="spin" /> Adding...
                    </>
                  ) : (
                    <>
                      <PlusCircle size={14} /> Add Question
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
