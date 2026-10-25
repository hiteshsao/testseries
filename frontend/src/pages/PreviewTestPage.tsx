import React, { useEffect, useState, useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import { adminApi, ApiError } from '../api/adminApi';
import { TestSeries, Question } from '../types';
import { PublishProblemsModal } from '../components/PublishProblemsModal';
import {
  ArrowLeft,
  Clock,
  CheckCircle,
  CheckCircle2,
  AlertTriangle,
  FileQuestion,
  ChevronLeft,
  ChevronRight,
  Eye,
  Edit3,
  Layers,
  BookOpen,
  ShieldCheck,
  RefreshCw,
  X
} from 'lucide-react';

export const PreviewTestPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const testId = Number(id);

  const [test, setTest] = useState<TestSeries | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Active question index for student CBT simulation (0-based)
  const [currentIndex, setCurrentIndex] = useState<number>(0);

  // View mode: 'CBT' (one-by-one student interface) or 'DOCUMENT' (all questions listed)
  const [viewMode, setViewMode] = useState<'CBT' | 'DOCUMENT'>('CBT');

  // Publish / Unpublish actions state
  const [isPublishing, setIsPublishing] = useState<boolean>(false);
  const [isUnpublishing, setIsUnpublishing] = useState<boolean>(false);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Publish problems modal state
  const [problemModal, setProblemModal] = useState<{
    isOpen: boolean;
    problems: string[];
  }>({
    isOpen: false,
    problems: [],
  });

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [testData, questionsData] = await Promise.all([
        adminApi.getTestById(testId),
        adminApi.getQuestions(testId),
      ]);
      setTest(testData);
      setQuestions(questionsData);
    } catch (err: any) {
      setError(err?.response?.message || err?.message || 'Failed to load preview data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (testId) {
      loadData();
    }
  }, [testId]);

  const showToast = (message: string, type: 'success' | 'error') => {
    setToast({ message, type });
    setTimeout(() => {
      setToast(null);
    }, 4500);
  };

  const handlePublish = async () => {
    if (!test) return;
    setIsPublishing(true);
    try {
      const updated = await adminApi.publishTest(test.id);
      setTest(updated);
      showToast(`Test "${updated.title}" published successfully!`, 'success');
    } catch (err: any) {
      if (err instanceof ApiError && err.response?.problems && err.response.problems.length > 0) {
        setProblemModal({
          isOpen: true,
          problems: err.response.problems,
        });
      } else {
        const fallbackMsg = err?.response?.message || err?.message || 'Failed to publish test.';
        setProblemModal({
          isOpen: true,
          problems: [fallbackMsg],
        });
      }
    } finally {
      setIsPublishing(false);
    }
  };

  const handleUnpublish = async () => {
    if (!test) return;
    setIsUnpublishing(true);
    try {
      const updated = await adminApi.unpublishTest(test.id);
      setTest(updated);
      showToast(`Test "${updated.title}" unpublished and moved back to DRAFT.`, 'success');
    } catch (err: any) {
      showToast(err?.response?.message || err?.message || 'Failed to unpublish test.', 'error');
    } finally {
      setIsUnpublishing(false);
    }
  };

  // Questions statistics
  const totalQuestions = questions.length;
  const questionsMissingAnswers = useMemo(() => {
    return questions.filter((q) => !q.correctOption || q.correctOption.trim() === '').length;
  }, [questions]);

  const currentQuestion = questions[currentIndex] || null;

  if (loading) {
    return (
      <div style={{ padding: '5rem 0', textAlign: 'center', color: 'var(--text-secondary)' }}>
        <RefreshCw size={28} className="spin" style={{ margin: '0 auto 1rem', display: 'block', color: 'var(--teal-primary)' }} />
        <p style={{ fontSize: '1rem', fontWeight: 500 }}>Generating Student Preview & Answer Key verification...</p>
      </div>
    );
  }

  if (error || !test) {
    return (
      <div style={{ maxWidth: '800px', margin: '2rem auto' }}>
        <div className="alert-box alert-error" style={{ marginBottom: '1.5rem' }}>
          <AlertTriangle size={20} style={{ flexShrink: 0 }} />
          <span>{error || 'Test not found.'}</span>
        </div>
        <Link to="/tests" className="btn-secondary" style={{ textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
          <ArrowLeft size={16} /> Back to Tests
        </Link>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1240px', margin: '0 auto', paddingBottom: '4rem' }}>
      {/* Toast Alert */}
      {toast && (
        <div
          style={{
            position: 'fixed',
            top: '1.5rem',
            right: '1.5rem',
            zIndex: 1200,
            display: 'flex',
            alignItems: 'center',
            gap: '0.75rem',
            padding: '0.85rem 1.25rem',
            backgroundColor: toast.type === 'success' ? 'rgba(16, 185, 129, 0.95)' : 'rgba(239, 68, 68, 0.95)',
            color: '#fff',
            borderRadius: 'var(--radius-md)',
            boxShadow: '0 10px 25px rgba(0, 0, 0, 0.4)',
            backdropFilter: 'blur(8px)',
            fontWeight: 500,
            fontSize: '0.9rem',
          }}
        >
          {toast.type === 'success' ? <CheckCircle2 size={18} /> : <AlertTriangle size={18} />}
          <span>{toast.message}</span>
          <button
            onClick={() => setToast(null)}
            style={{ background: 'none', border: 'none', color: '#fff', cursor: 'pointer', padding: 0 }}
          >
            <X size={14} />
          </button>
        </div>
      )}

      {/* Admin Preview Notice Banner */}
      <div
        id="preview-admin-banner"
        style={{
          backgroundColor: 'rgba(20, 184, 166, 0.1)',
          border: '1px solid var(--border-active)',
          borderRadius: 'var(--radius-md)',
          padding: '0.85rem 1.25rem',
          marginBottom: '1.25rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1rem',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div
            style={{
              width: '36px',
              height: '36px',
              borderRadius: 'var(--radius-sm)',
              backgroundColor: 'var(--teal-primary)',
              color: '#fff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <Eye size={18} />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span style={{ fontSize: '0.92rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                Test Preview Mode (Read-Only)
              </span>
              <span
                style={{
                  fontSize: '0.72rem',
                  backgroundColor: 'rgba(16, 185, 129, 0.2)',
                  color: '#34d399',
                  padding: '2px 8px',
                  borderRadius: '999px',
                  fontWeight: 600,
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '4px',
                }}
              >
                <ShieldCheck size={11} /> Admin Answer Key Visible
              </span>
            </div>
            <p style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', marginTop: '0.15rem' }}>
              This simulates the candidate's exam screen. As an administrator, you see a prominent green marker on the correct option configured in the answer key.
            </p>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <button
            type="button"
            className={`btn-sm ${viewMode === 'CBT' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setViewMode('CBT')}
            title="Single-question interactive CBT exam view"
            id="btn-view-cbt"
          >
            <BookOpen size={13} />
            <span>CBT Exam View</span>
          </button>
          <button
            type="button"
            className={`btn-sm ${viewMode === 'DOCUMENT' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setViewMode('DOCUMENT')}
            title="All questions full document view"
            id="btn-view-document"
          >
            <Layers size={13} />
            <span>All Questions</span>
          </button>
        </div>
      </div>

      {/* Main Header & Actions */}
      <div
        className="page-header"
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '1.25rem',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.35rem' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
              Test #{test.id}
            </span>
            <span className={`status-pill ${test.status === 'PUBLISHED' ? 'status-published' : 'status-draft'}`}>
              {test.status === 'PUBLISHED' ? <CheckCircle size={12} /> : null}
              {test.status}
            </span>
            <span className={`access-badge ${test.accessType === 'PAID' ? 'access-paid' : 'access-free'}`}>
              {test.accessType === 'PAID' ? `₹${Number(test.price).toFixed(2)}` : 'FREE'}
            </span>
          </div>
          <h1 className="page-title">{test.title}</h1>
          <p className="page-subtitle">
            {test.instructions || 'Standard examination instructions apply.'}
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', flexWrap: 'wrap' }}>
          {/* Link back to Questions Review */}
          <Link
            to={`/tests/${test.id}/questions`}
            className="btn-secondary"
            style={{ textDecoration: 'none' }}
            id="btn-preview-back-questions"
          >
            <Edit3 size={15} /> Edit Questions
          </Link>

          {/* Link back to Tests list */}
          <Link
            to="/tests"
            className="btn-secondary"
            style={{ textDecoration: 'none' }}
            id="btn-preview-back-tests"
          >
            <ArrowLeft size={15} /> Tests List
          </Link>

          {/* Publish / Unpublish Action Button */}
          {test.status === 'DRAFT' ? (
            <button
              type="button"
              className="btn-primary"
              style={{
                backgroundColor: 'var(--teal-primary)',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.5rem',
              }}
              disabled={isPublishing}
              onClick={handlePublish}
              id="btn-preview-publish"
            >
              <CheckCircle size={15} />
              <span>{isPublishing ? 'Validating & Publishing...' : 'Publish Test'}</span>
            </button>
          ) : (
            <button
              type="button"
              className="btn-secondary"
              style={{
                borderColor: 'rgba(239, 68, 68, 0.4)',
                color: '#f87171',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.5rem',
              }}
              disabled={isUnpublishing}
              onClick={handleUnpublish}
              id="btn-preview-unpublish"
            >
              <AlertTriangle size={15} />
              <span>{isUnpublishing ? 'Unpublishing...' : 'Unpublish'}</span>
            </button>
          )}
        </div>
      </div>

      {/* Warnings & Readiness Banner */}
      {questionsMissingAnswers > 0 && (
        <div
          id="preview-missing-answers-banner"
          style={{
            backgroundColor: 'rgba(245, 158, 11, 0.1)',
            border: '1px solid rgba(245, 158, 11, 0.35)',
            borderRadius: 'var(--radius-md)',
            padding: '0.85rem 1.25rem',
            marginBottom: '1.5rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '1rem',
            flexWrap: 'wrap',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <AlertTriangle size={20} color="#f59e0b" style={{ flexShrink: 0 }} />
            <span style={{ fontSize: '0.88rem', color: '#fde68a' }}>
              <strong>Publish Warning:</strong> {questionsMissingAnswers} out of {totalQuestions} questions do not have a correct answer marked. Publishing will fail with HTTP 400 until all questions are answered.
            </span>
          </div>
          <Link
            to={`/tests/${test.id}/questions`}
            className="btn-sm"
            style={{
              backgroundColor: '#f59e0b',
              color: '#070b14',
              fontWeight: 600,
              textDecoration: 'none',
              padding: '0.35rem 0.75rem',
              borderRadius: 'var(--radius-sm)',
            }}
          >
            Fix in Review
          </Link>
        </div>
      )}

      {/* Simulated Student Exam Top Navigation Bar */}
      <div
        id="student-exam-top-bar"
        style={{
          backgroundColor: 'var(--bg-sidebar)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-md) var(--radius-md) 0 0',
          padding: '0.85rem 1.5rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1rem',
          borderBottom: '1px solid var(--border-subtle)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          <div>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Section
            </span>
            <div style={{ fontSize: '0.92rem', fontWeight: 700, color: 'var(--teal-primary)' }}>
              All Questions ({totalQuestions})
            </div>
          </div>
          <div style={{ width: '1px', height: '24px', backgroundColor: 'var(--border-subtle)' }} />
          <div>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Total Duration
            </span>
            <div style={{ fontSize: '0.92rem', fontWeight: 600, color: 'var(--text-primary)' }}>
              {test.durationMinutes} Minutes
            </div>
          </div>
        </div>

        {/* Simulated Student Timer */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.75rem',
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border-active)',
            padding: '0.45rem 0.95rem',
            borderRadius: 'var(--radius-sm)',
          }}
        >
          <Clock size={16} color="var(--teal-primary)" />
          <div style={{ textAlign: 'right' }}>
            <span style={{ fontSize: '0.68rem', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase' }}>
              Simulated Timer
            </span>
            <span style={{ fontSize: '0.95rem', fontFamily: 'monospace', fontWeight: 700, color: 'var(--text-primary)' }}>
              {String(Math.floor(test.durationMinutes / 60)).padStart(2, '0')}:
              {String(test.durationMinutes % 60).padStart(2, '0')}:00
            </span>
          </div>
        </div>
      </div>

      {/* If test has no questions */}
      {questions.length === 0 ? (
        <div
          style={{
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderRadius: '0 0 var(--radius-md) var(--radius-md)',
            padding: '4rem 2rem',
            textAlign: 'center',
          }}
        >
          <FileQuestion size={44} color="var(--text-muted)" style={{ margin: '0 auto 1rem', display: 'block' }} />
          <h3 style={{ fontSize: '1.15rem', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
            No Questions Found in this Test
          </h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', maxWidth: '480px', margin: '0 auto 1.5rem' }}>
            This test has no questions yet. You can extract questions automatically by uploading a PDF, or create questions manually.
          </p>
          <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
            <Link to={`/tests/${test.id}/upload`} className="btn-primary" style={{ textDecoration: 'none' }}>
              Upload PDF
            </Link>
            <Link to={`/tests/${test.id}/questions`} className="btn-secondary" style={{ textDecoration: 'none' }}>
              Add Question Manually
            </Link>
          </div>
        </div>
      ) : viewMode === 'CBT' ? (
        /* ======================== CBT MODE ======================== */
        <div
          id="preview-cbt-container"
          style={{
            display: 'grid',
            gridTemplateColumns: 'minmax(0, 1fr) 300px',
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderTop: 'none',
            borderRadius: '0 0 var(--radius-md) var(--radius-md)',
            minHeight: '520px',
          }}
        >
          {/* Question Main Panel */}
          {currentQuestion && (
            <div
              style={{
                padding: '2rem',
                borderRight: '1px solid var(--border-subtle)',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
              }}
            >
              <div>
                {/* Question Info & Marks Header */}
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    paddingBottom: '1rem',
                    borderBottom: '1px solid var(--border-subtle)',
                    marginBottom: '1.5rem',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                    <span
                      style={{
                        backgroundColor: 'var(--teal-glow)',
                        color: 'var(--teal-primary)',
                        padding: '4px 10px',
                        borderRadius: 'var(--radius-sm)',
                        fontWeight: 700,
                        fontSize: '0.9rem',
                      }}
                    >
                      Question {currentQuestion.orderNo}
                    </span>
                    <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                      (Q {currentIndex + 1} of {totalQuestions})
                    </span>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <span
                      style={{
                        fontSize: '0.8rem',
                        backgroundColor: 'rgba(16, 185, 129, 0.12)',
                        color: '#34d399',
                        padding: '3px 8px',
                        borderRadius: '4px',
                        fontWeight: 600,
                      }}
                    >
                      +{currentQuestion.marks} Marks
                    </span>
                    <span
                      style={{
                        fontSize: '0.8rem',
                        backgroundColor: 'rgba(239, 68, 68, 0.12)',
                        color: '#f87171',
                        padding: '3px 8px',
                        borderRadius: '4px',
                        fontWeight: 600,
                      }}
                    >
                      -{currentQuestion.negativeMarks} Negative
                    </span>
                  </div>
                </div>

                {/* Missing Answer Key Alert on Question */}
                {(!currentQuestion.correctOption || currentQuestion.correctOption.trim() === '') && (
                  <div
                    style={{
                      backgroundColor: 'rgba(245, 158, 11, 0.12)',
                      border: '1px solid rgba(245, 158, 11, 0.4)',
                      borderRadius: 'var(--radius-sm)',
                      padding: '0.65rem 0.95rem',
                      marginBottom: '1.25rem',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.65rem',
                      fontSize: '0.82rem',
                      color: '#fde68a',
                    }}
                  >
                    <AlertTriangle size={15} color="#f59e0b" style={{ flexShrink: 0 }} />
                    <span>
                      <strong>Admin Notice:</strong> No correct answer configured for this question.
                    </span>
                    <Link
                      to={`/tests/${test.id}/questions`}
                      style={{ marginLeft: 'auto', color: 'var(--teal-primary)', textDecoration: 'underline', fontSize: '0.78rem' }}
                    >
                      Set Answer
                    </Link>
                  </div>
                )}

                {/* Question Prompt */}
                <div
                  style={{
                    fontSize: '1.05rem',
                    lineHeight: '1.7',
                    color: 'var(--text-primary)',
                    marginBottom: '2rem',
                    whiteSpace: 'pre-wrap',
                    fontWeight: 500,
                  }}
                >
                  {currentQuestion.text}
                </div>

                {/* Options List (Read-Only Student Cards with Admin Correct Marker) */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem', marginBottom: '2rem' }}>
                  {(['A', 'B', 'C', 'D'] as const).map((optKey) => {
                    const optText = currentQuestion[`option${optKey}` as keyof Question] as string;
                    const isCorrect = currentQuestion.correctOption === optKey;

                    return (
                      <div
                        key={optKey}
                        id={`question-${currentQuestion.id}-option-${optKey}`}
                        style={{
                          display: 'flex',
                          alignItems: 'flex-start',
                          gap: '1rem',
                          padding: '1rem 1.25rem',
                          backgroundColor: isCorrect
                            ? 'rgba(16, 185, 129, 0.1)'
                            : 'var(--bg-input)',
                          border: isCorrect
                            ? '2px solid #10b981'
                            : '1px solid var(--border-subtle)',
                          borderRadius: 'var(--radius-md)',
                          transition: 'all 0.15s ease',
                          boxShadow: isCorrect ? '0 0 14px rgba(16, 185, 129, 0.2)' : 'none',
                          position: 'relative',
                        }}
                      >
                        {/* Radio circle (read-only) */}
                        <div
                          style={{
                            width: '24px',
                            height: '24px',
                            borderRadius: '50%',
                            border: isCorrect ? '2px solid #10b981' : '2px solid var(--border-subtle)',
                            backgroundColor: isCorrect ? '#10b981' : 'transparent',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            color: '#fff',
                            flexShrink: 0,
                            marginTop: '0.1rem',
                          }}
                        >
                          {isCorrect && <CheckCircle2 size={15} color="#fff" />}
                        </div>

                        {/* Option Identifier Badge */}
                        <div
                          style={{
                            fontWeight: 700,
                            fontSize: '0.9rem',
                            color: isCorrect ? '#34d399' : 'var(--text-secondary)',
                            width: '20px',
                            flexShrink: 0,
                            marginTop: '0.15rem',
                          }}
                        >
                          ({optKey})
                        </div>

                        {/* Option Text */}
                        <div
                          style={{
                            flex: 1,
                            fontSize: '0.95rem',
                            color: isCorrect ? 'var(--text-primary)' : 'var(--text-secondary)',
                            lineHeight: 1.5,
                            whiteSpace: 'pre-wrap',
                          }}
                        >
                          {optText}
                        </div>

                        {/* ADMIN MARKER BADGE */}
                        {isCorrect && (
                          <div
                            id={`admin-marker-${currentQuestion.id}-${optKey}`}
                            style={{
                              backgroundColor: 'rgba(16, 185, 129, 0.22)',
                              border: '1px solid #10b981',
                              color: '#34d399',
                              padding: '4px 10px',
                              borderRadius: '999px',
                              fontSize: '0.74rem',
                              fontWeight: 700,
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '5px',
                              flexShrink: 0,
                              textTransform: 'uppercase',
                              letterSpacing: '0.04em',
                            }}
                          >
                            <CheckCircle size={12} />
                            <span>Correct Option (Admin Marker)</span>
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Student Exam Bottom Navigation Controls */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  paddingTop: '1.25rem',
                  borderTop: '1px solid var(--border-subtle)',
                  marginTop: '1rem',
                }}
              >
                <button
                  type="button"
                  className="btn-secondary"
                  disabled={currentIndex === 0}
                  onClick={() => setCurrentIndex((prev) => Math.max(0, prev - 1))}
                  id="btn-prev-question"
                >
                  <ChevronLeft size={16} /> Previous
                </button>

                <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                  Question {currentIndex + 1} of {totalQuestions}
                </div>

                <button
                  type="button"
                  className="btn-primary"
                  disabled={currentIndex === totalQuestions - 1}
                  onClick={() => setCurrentIndex((prev) => Math.min(totalQuestions - 1, prev + 1))}
                  id="btn-next-question"
                >
                  <span>Next</span> <ChevronRight size={16} />
                </button>
              </div>
            </div>
          )}

          {/* Right Question Palette Sidebar */}
          <div
            style={{
              padding: '1.5rem',
              backgroundColor: 'rgba(7, 11, 20, 0.5)',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
            }}
          >
            <div>
              <div
                style={{
                  fontSize: '0.85rem',
                  fontWeight: 700,
                  color: 'var(--text-primary)',
                  textTransform: 'uppercase',
                  letterSpacing: '0.05em',
                  marginBottom: '1rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                }}
              >
                <span>Question Palette</span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  {totalQuestions} Qs
                </span>
              </div>

              {/* Grid of Question Numbers */}
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(5, 1fr)',
                  gap: '0.5rem',
                  maxHeight: '340px',
                  overflowY: 'auto',
                  paddingRight: '0.25rem',
                }}
                id="question-palette-grid"
              >
                {questions.map((q, idx) => {
                  const isCurrent = idx === currentIndex;
                  const hasAnswer = q.correctOption && q.correctOption.trim() !== '';

                  let bg = 'var(--bg-card)';
                  let border = '1px solid var(--border-subtle)';
                  let color = 'var(--text-secondary)';

                  if (isCurrent) {
                    bg = 'var(--teal-primary)';
                    border = '2px solid #2dd4bf';
                    color = '#fff';
                  } else if (hasAnswer) {
                    border = '1px solid rgba(16, 185, 129, 0.45)';
                    color = '#34d399';
                  } else {
                    border = '1px solid rgba(245, 158, 11, 0.45)';
                    color = '#f59e0b';
                  }

                  return (
                    <button
                      key={q.id}
                      type="button"
                      onClick={() => setCurrentIndex(idx)}
                      id={`palette-btn-${q.orderNo}`}
                      style={{
                        height: '38px',
                        backgroundColor: bg,
                        border: border,
                        color: color,
                        borderRadius: 'var(--radius-sm)',
                        fontWeight: 700,
                        fontSize: '0.85rem',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        transition: 'all 0.15s ease',
                      }}
                      title={`Question ${q.orderNo}${hasAnswer ? ` (Answer: ${q.correctOption})` : ' (No Answer!)'}`}
                    >
                      {q.orderNo}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Palette Legend */}
            <div
              style={{
                marginTop: '1.5rem',
                paddingTop: '1rem',
                borderTop: '1px solid var(--border-subtle)',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.5rem',
                fontSize: '0.75rem',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#34d399' }}>
                <div style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#10b981' }} />
                <span>Answer key configured ({totalQuestions - questionsMissingAnswers})</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#f59e0b' }}>
                <div style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#f59e0b' }} />
                <span>Missing answer key ({questionsMissingAnswers})</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-primary)' }}>
                <div style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: 'var(--teal-primary)' }} />
                <span>Current Question</span>
              </div>
            </div>
          </div>
        </div>
      ) : (
        /* ======================== DOCUMENT VIEW MODE ======================== */
        <div
          id="preview-document-container"
          style={{
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderTop: 'none',
            borderRadius: '0 0 var(--radius-md) var(--radius-md)',
            padding: '2rem',
            display: 'flex',
            flexDirection: 'column',
            gap: '2.5rem',
          }}
        >
          {questions.map((q) => {
            const hasAnswer = q.correctOption && q.correctOption.trim() !== '';

            return (
              <div
                key={q.id}
                id={`document-question-${q.id}`}
                style={{
                  paddingBottom: '2rem',
                  borderBottom: '1px solid var(--border-subtle)',
                }}
              >
                {/* Header */}
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    marginBottom: '1rem',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <span
                      style={{
                        backgroundColor: 'var(--teal-glow)',
                        color: 'var(--teal-primary)',
                        padding: '3px 10px',
                        borderRadius: 'var(--radius-sm)',
                        fontWeight: 700,
                        fontSize: '0.85rem',
                      }}
                    >
                      Question {q.orderNo}
                    </span>
                    {!hasAnswer && (
                      <span className="badge-amber" style={{ fontSize: '0.74rem' }}>
                        <AlertTriangle size={12} /> No correct answer set
                      </span>
                    )}
                  </div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                    +{q.marks} / -{q.negativeMarks} Marks
                  </div>
                </div>

                {/* Prompt */}
                <div
                  style={{
                    fontSize: '1rem',
                    lineHeight: 1.6,
                    color: 'var(--text-primary)',
                    marginBottom: '1.25rem',
                    whiteSpace: 'pre-wrap',
                  }}
                >
                  {q.text}
                </div>

                {/* Options 2x2 Grid */}
                <div
                  style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
                    gap: '0.75rem',
                  }}
                >
                  {(['A', 'B', 'C', 'D'] as const).map((optKey) => {
                    const optText = q[`option${optKey}` as keyof Question] as string;
                    const isCorrect = q.correctOption === optKey;

                    return (
                      <div
                        key={optKey}
                        style={{
                          display: 'flex',
                          alignItems: 'flex-start',
                          gap: '0.75rem',
                          padding: '0.75rem 1rem',
                          backgroundColor: isCorrect ? 'rgba(16, 185, 129, 0.1)' : 'var(--bg-input)',
                          border: isCorrect ? '1.5px solid #10b981' : '1px solid var(--border-subtle)',
                          borderRadius: 'var(--radius-sm)',
                          position: 'relative',
                        }}
                      >
                        <span
                          style={{
                            fontWeight: 700,
                            fontSize: '0.85rem',
                            color: isCorrect ? '#34d399' : 'var(--text-secondary)',
                          }}
                        >
                          ({optKey})
                        </span>
                        <span
                          style={{
                            fontSize: '0.88rem',
                            color: isCorrect ? 'var(--text-primary)' : 'var(--text-secondary)',
                            flex: 1,
                          }}
                        >
                          {optText}
                        </span>
                        {isCorrect && (
                          <span
                            style={{
                              backgroundColor: 'rgba(16, 185, 129, 0.2)',
                              color: '#34d399',
                              padding: '2px 6px',
                              borderRadius: '4px',
                              fontSize: '0.68rem',
                              fontWeight: 700,
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '3px',
                            }}
                          >
                            <CheckCircle size={10} /> Correct
                          </span>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Publish Failure Problems Modal */}
      <PublishProblemsModal
        isOpen={problemModal.isOpen}
        onClose={() => setProblemModal((prev) => ({ ...prev, isOpen: false }))}
        problems={problemModal.problems}
        testTitle={test.title}
        testId={test.id}
      />
    </div>
  );
};
