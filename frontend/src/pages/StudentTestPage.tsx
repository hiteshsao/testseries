import React, { useEffect, useState, useRef, useMemo, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { studentApi } from '../api/studentApi';
import {
  StudentAttemptResponse,
  StudentQuestion,
  StudentSubmitResult
} from '../types';
import {
  Clock,
  CheckCircle2,
  AlertTriangle,
  FileQuestion,
  ChevronLeft,
  ChevronRight,
  Bookmark,
  Check,
  X,
  Menu,
  ShieldAlert,
  Award,
  RefreshCw,
  RotateCcw
} from 'lucide-react';

export const StudentTestPage: React.FC = () => {
  const { id, attemptId: routeAttemptId } = useParams<{ id: string; attemptId?: string }>();
  const testId = Number(id);

  // Core Attempt Data
  const [attempt, setAttempt] = useState<StudentAttemptResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Active question navigation index (0-based)
  const [currentIndex, setCurrentIndex] = useState<number>(0);

  // Local state for answers mapping: questionId -> { selectedOption: string | null, markedForReview: boolean }
  const [answersMap, setAnswersMap] = useState<Record<number, { selectedOption: string | null; markedForReview: boolean }>>({});

  // Autosave status: 'saved' | 'saving' | 'error'
  const [autosaveStatus, setAutosaveStatus] = useState<'saved' | 'saving' | 'error'>('saved');

  // Mobile Question Palette Drawer state
  const [paletteDrawerOpen, setPaletteDrawerOpen] = useState<boolean>(false);

  // Submit Modal state
  const [showSubmitModal, setShowSubmitModal] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const submittingRef = useRef<boolean>(false);

  // Auto-submit countdown trigger state
  const [autoSubmitting, setAutoSubmitting] = useState<boolean>(false);

  // Submission Result state
  const [submitResult, setSubmitResult] = useState<StudentSubmitResult | null>(null);

  // Server-driven Countdown Timer (in seconds)
  const [remainingSeconds, setRemainingSeconds] = useState<number | null>(null);

  // Load Attempt (by existing routeAttemptId or by starting/resuming for testId)
  useEffect(() => {
    let isMounted = true;

    const loadAttempt = async () => {
      setLoading(true);
      setError(null);
      try {
        let attemptData: StudentAttemptResponse;

        if (routeAttemptId) {
          attemptData = await studentApi.getAttempt(Number(routeAttemptId));
        } else if (testId) {
          attemptData = await studentApi.startTest(testId);
        } else {
          throw new Error('Test ID or Attempt ID is required.');
        }

        if (!isMounted) return;

        setAttempt(attemptData);

        // Populate local answers map
        const initialMap: Record<number, { selectedOption: string | null; markedForReview: boolean }> = {};
        if (attemptData.questions) {
          attemptData.questions.forEach((q) => {
            initialMap[q.id] = { selectedOption: null, markedForReview: false };
          });
        }
        if (attemptData.answers) {
          attemptData.answers.forEach((ans) => {
            initialMap[ans.questionId] = {
              selectedOption: ans.selectedOption || null,
              markedForReview: !!ans.markedForReview,
            };
          });
        }
        setAnswersMap(initialMap);

        // Calculate initial remaining seconds from server endsAt
        const endsAtMs = new Date(attemptData.endsAt).getTime();
        const diffSec = Math.max(0, Math.floor((endsAtMs - Date.now()) / 1000));
        setRemainingSeconds(diffSec);

        // If attempt is already SUBMITTED, load result
        if (attemptData.status === 'SUBMITTED') {
          try {
            const res = await studentApi.submitAttempt(attemptData.attemptId);
            setSubmitResult(res);
          } catch {
            // Already submitted view
          }
        }
      } catch (err: any) {
        if (!isMounted) return;
        setError(err?.response?.message || err?.message || 'Failed to initialize test attempt.');
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    loadAttempt();

    return () => {
      isMounted = false;
    };
  }, [testId, routeAttemptId]);

  // Submit test logic with Double Submit Prevention
  const performSubmit = useCallback(async (isAuto = false) => {
    if (!attempt || submittingRef.current) return;

    // Lock double submissions
    submittingRef.current = true;
    setIsSubmitting(true);
    if (isAuto) setAutoSubmitting(true);

    try {
      const result = await studentApi.submitAttempt(attempt.attemptId);
      setSubmitResult(result);
      setShowSubmitModal(false);
    } catch (err: any) {
      alert(err?.response?.message || err?.message || 'Submission failed. Please check network and retry.');
      submittingRef.current = false;
      setIsSubmitting(false);
      setAutoSubmitting(false);
    }
  }, [attempt]);

  // Server-driven Countdown Timer Tick
  useEffect(() => {
    if (!attempt || attempt.status === 'SUBMITTED' || submitResult) return;

    const interval = setInterval(() => {
      const endsAtMs = new Date(attempt.endsAt).getTime();
      const diffSec = Math.max(0, Math.floor((endsAtMs - Date.now()) / 1000));
      setRemainingSeconds(diffSec);

      // Trigger auto-submit when countdown hits 0
      if (diffSec <= 0 && !submittingRef.current) {
        clearInterval(interval);
        performSubmit(true);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [attempt, submitResult, performSubmit]);

  // Format seconds to HH:MM:SS
  const formatTimer = (secs: number | null): string => {
    if (secs === null) return '--:--:--';
    const hours = Math.floor(secs / 3600);
    const minutes = Math.floor((secs % 3600) / 60);
    const seconds = secs % 60;

    const pad = (n: number) => String(n).padStart(2, '0');
    if (hours > 0) {
      return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
    }
    return `${pad(minutes)}:${pad(seconds)}`;
  };

  // Questions array
  const questions: StudentQuestion[] = useMemo(() => attempt?.questions || [], [attempt]);
  const currentQuestion: StudentQuestion | undefined = questions[currentIndex];

  // Autosave single answer via API
  const persistAnswer = async (
    questionId: number,
    selectedOption: string | null,
    markedForReview: boolean
  ) => {
    if (!attempt || attempt.status === 'SUBMITTED' || submittingRef.current) return;

    setAutosaveStatus('saving');
    try {
      await studentApi.saveAnswer(attempt.attemptId, {
        questionId,
        selectedOption,
        markedForReview,
      });
      setAutosaveStatus('saved');
    } catch (err) {
      console.error('Autosave error:', err);
      setAutosaveStatus('error');
    }
  };

  // Handle option selection (toggle or set)
  const handleSelectOption = (optionKey: string) => {
    if (!currentQuestion || isSubmitting || submitResult) return;

    const currentAns = answersMap[currentQuestion.id] || { selectedOption: null, markedForReview: false };
    const newSelected = currentAns.selectedOption === optionKey ? null : optionKey;

    const updated = {
      ...answersMap,
      [currentQuestion.id]: {
        selectedOption: newSelected,
        markedForReview: currentAns.markedForReview,
      },
    };

    setAnswersMap(updated);
    persistAnswer(currentQuestion.id, newSelected, currentAns.markedForReview);
  };

  // Clear current response
  const handleClearResponse = () => {
    if (!currentQuestion || isSubmitting || submitResult) return;

    const currentAns = answersMap[currentQuestion.id] || { selectedOption: null, markedForReview: false };
    if (!currentAns.selectedOption) return;

    const updated = {
      ...answersMap,
      [currentQuestion.id]: {
        selectedOption: null,
        markedForReview: currentAns.markedForReview,
      },
    };

    setAnswersMap(updated);
    persistAnswer(currentQuestion.id, null, currentAns.markedForReview);
  };

  // Toggle Mark for Review
  const handleToggleMarkForReview = () => {
    if (!currentQuestion || isSubmitting || submitResult) return;

    const currentAns = answersMap[currentQuestion.id] || { selectedOption: null, markedForReview: false };
    const newMarked = !currentAns.markedForReview;

    const updated = {
      ...answersMap,
      [currentQuestion.id]: {
        selectedOption: currentAns.selectedOption,
        markedForReview: newMarked,
      },
    };

    setAnswersMap(updated);
    persistAnswer(currentQuestion.id, currentAns.selectedOption, newMarked);
  };

  // Compute question palette status
  const getQuestionStatus = (qId: number) => {
    const ans = answersMap[qId];
    if (!ans) return 'not-answered';

    const hasOption = ans.selectedOption !== null && ans.selectedOption !== undefined && ans.selectedOption !== '';
    const isMarked = !!ans.markedForReview;

    if (hasOption && isMarked) return 'marked-answered';
    if (isMarked) return 'marked';
    if (hasOption) return 'answered';
    return 'not-answered';
  };

  // Compute counts for palette and confirm dialog
  const counts = useMemo(() => {
    let answered = 0;
    let notAnswered = 0;
    let marked = 0;
    let markedAnswered = 0;

    questions.forEach((q) => {
      const status = getQuestionStatus(q.id);
      if (status === 'answered') answered++;
      else if (status === 'not-answered') notAnswered++;
      else if (status === 'marked') marked++;
      else if (status === 'marked-answered') markedAnswered++;
    });

    return {
      total: questions.length,
      answered,
      notAnswered,
      marked,
      markedAnswered,
      totalAnswered: answered + markedAnswered,
    };
  }, [questions, answersMap]);

  if (loading) {
    return (
      <div className="test-screen-container" style={{ alignItems: 'center', justifyContent: 'center' }}>
        <div className="btn-spinner" style={{ width: 44, height: 44, borderWidth: 3, marginBottom: '1.5rem' }} />
        <h2 style={{ fontSize: '1.3rem', color: 'var(--text-primary)' }}>Loading Examination Environment...</h2>
        <p style={{ color: 'var(--text-secondary)', marginTop: '0.5rem' }}>Synchronizing test timer and questions with server.</p>
      </div>
    );
  }

  if (error || !attempt) {
    return (
      <div className="test-screen-container" style={{ alignItems: 'center', justifyContent: 'center', padding: '2rem' }}>
        <div className="instruction-rules-card" style={{ maxWidth: 520, textAlign: 'center', borderColor: 'rgba(239, 68, 68, 0.4)' }}>
          <AlertTriangle size={48} color="#ef4444" style={{ margin: '0 auto 1.25rem' }} />
          <h2 style={{ fontSize: '1.35rem', color: 'var(--text-primary)', marginBottom: '0.75rem' }}>
            Cannot Access Test Attempt
          </h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '1.75rem', lineHeight: 1.5 }}>
            {error || 'Unable to load test. Your session may have expired or max attempts reached.'}
          </p>
          <Link to="/tests" className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
            <ChevronLeft size={16} />
            Return to Available Tests
          </Link>
        </div>
      </div>
    );
  }

  // Timer visual urgency classes
  const isTimerUrgent = remainingSeconds !== null && remainingSeconds <= 60;
  const isTimerWarning = remainingSeconds !== null && remainingSeconds <= 300 && remainingSeconds > 60;

  return (
    <div className="test-screen-container">
      {/* CBT Fixed Top Bar */}
      <header className="test-topbar">
        {/* Left: Test Title & Autosave Pill */}
        <div className="test-topbar-left">
          <h2 className="test-title-heading" title={attempt.testTitle}>
            {attempt.testTitle}
          </h2>

          <div
            className={`autosave-pill ${autosaveStatus === 'saving' ? 'saving' : autosaveStatus === 'error' ? 'error' : ''}`}
            title="Auto-saves response immediately to server clock"
          >
            {autosaveStatus === 'saving' ? (
              <>
                <RefreshCw size={12} className="btn-spinner" style={{ width: 12, height: 12 }} />
                <span>Saving...</span>
              </>
            ) : autosaveStatus === 'error' ? (
              <>
                <AlertTriangle size={12} />
                <span>Save failed - retry</span>
              </>
            ) : (
              <>
                <Check size={12} />
                <span>All answers saved</span>
              </>
            )}
          </div>
        </div>

        {/* Center: Countdown Timer driven by server ends_at */}
        <div className="test-topbar-center">
          <div
            id="cbt-countdown-timer"
            className={`cbt-timer ${isTimerUrgent ? 'urgent' : isTimerWarning ? 'warning' : ''}`}
            title="Countdown driven by server ends_at clock"
          >
            <Clock size={18} />
            <span>{formatTimer(remainingSeconds)}</span>
          </div>
        </div>

        {/* Right: Mobile Palette Drawer Toggle & Submit Button */}
        <div className="test-topbar-right">
          <button
            type="button"
            id="btn-open-palette-drawer"
            className="btn-mobile-palette"
            onClick={() => setPaletteDrawerOpen(true)}
            aria-label="Open Question Palette Drawer"
          >
            <Menu size={16} />
            <span>Palette ({counts.totalAnswered}/{counts.total})</span>
          </button>

          <button
            type="button"
            id="btn-submit-test-topbar"
            className="btn btn-primary"
            style={{ padding: '0.45rem 1.15rem', fontSize: '0.88rem' }}
            disabled={isSubmitting || !!submitResult}
            onClick={() => setShowSubmitModal(true)}
          >
            <CheckCircle2 size={16} />
            <span>Submit Test</span>
          </button>
        </div>
      </header>

      {/* Main Workspace (Split View) */}
      <div className="test-workspace">
        {/* Left Column: Active Question Viewport */}
        <main className="test-question-area">
          {currentQuestion ? (
            <>
              {/* Question Header Bar: Number + Marks + Review Toggle */}
              <div className="question-header-bar">
                <div className="question-num-tag">
                  <span>Question {String(currentIndex + 1).padStart(2, '0')}</span>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 400 }}>
                    of {questions.length}
                  </span>
                </div>

                <div className="question-marks-badges">
                  {currentQuestion.marks != null && (
                    <span className="badge-pos-mark">+{Number(currentQuestion.marks).toFixed(1)} Mark</span>
                  )}
                  {currentQuestion.negativeMarks != null && Number(currentQuestion.negativeMarks) > 0 && (
                    <span className="badge-neg-mark">-{Number(currentQuestion.negativeMarks).toFixed(1)} Neg</span>
                  )}

                  {/* Mark for Review Toggle Button */}
                  <button
                    type="button"
                    id="btn-mark-for-review"
                    className={`btn-mark-review ${answersMap[currentQuestion.id]?.markedForReview ? 'active' : ''}`}
                    onClick={handleToggleMarkForReview}
                    title="Flag this question to review before submitting"
                  >
                    <Bookmark size={14} />
                    <span>
                      {answersMap[currentQuestion.id]?.markedForReview ? 'Marked for Review' : 'Mark for Review'}
                    </span>
                  </button>
                </div>
              </div>

              {/* Question Text */}
              <div className="question-text-box">
                {currentQuestion.text}
              </div>

              {/* Options Cards (A, B, C, D) */}
              <div className="options-container">
                {[
                  { key: 'A', text: currentQuestion.optionA },
                  { key: 'B', text: currentQuestion.optionB },
                  { key: 'C', text: currentQuestion.optionC },
                  { key: 'D', text: currentQuestion.optionD },
                ].map((opt) => {
                  const isSelected = answersMap[currentQuestion.id]?.selectedOption === opt.key;
                  return (
                    <button
                      key={opt.key}
                      type="button"
                      id={`option-${opt.key.toLowerCase()}`}
                      className={`option-select-btn ${isSelected ? 'selected' : ''}`}
                      onClick={() => handleSelectOption(opt.key)}
                    >
                      <div className="option-badge-key">
                        {isSelected ? <Check size={14} /> : opt.key}
                      </div>
                      <div className="option-text-content">
                        {opt.text}
                      </div>
                    </button>
                  );
                })}
              </div>

              {/* Bottom Question Navigation Footer */}
              <footer className="question-nav-footer">
                <button
                  type="button"
                  id="btn-clear-response"
                  className="btn btn-secondary"
                  style={{ fontSize: '0.85rem' }}
                  disabled={!answersMap[currentQuestion.id]?.selectedOption}
                  onClick={handleClearResponse}
                >
                  <RotateCcw size={14} />
                  Clear Response
                </button>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <button
                    type="button"
                    id="btn-prev-question"
                    className="btn btn-secondary"
                    disabled={currentIndex === 0}
                    onClick={() => setCurrentIndex((prev) => Math.max(0, prev - 1))}
                  >
                    <ChevronLeft size={16} />
                    Previous
                  </button>

                  {currentIndex < questions.length - 1 ? (
                    <button
                      type="button"
                      id="btn-next-question"
                      className="btn btn-primary"
                      onClick={() => setCurrentIndex((prev) => Math.min(questions.length - 1, prev + 1))}
                    >
                      Next
                      <ChevronRight size={16} />
                    </button>
                  ) : (
                    <button
                      type="button"
                      id="btn-review-submit"
                      className="btn btn-primary"
                      onClick={() => setShowSubmitModal(true)}
                    >
                      Review & Submit
                      <CheckCircle2 size={16} />
                    </button>
                  )}
                </div>
              </footer>
            </>
          ) : (
            <div style={{ textAlign: 'center', padding: '3rem' }}>
              <FileQuestion size={40} color="var(--text-secondary)" style={{ margin: '0 auto 1rem' }} />
              <p style={{ color: 'var(--text-secondary)' }}>No questions available for this test.</p>
            </div>
          )}
        </main>

        {/* Right Column: Desktop Question Palette */}
        <aside className="palette-sidebar">
          <div className="palette-header">
            <div className="palette-title">Question Palette</div>
            <div className="palette-stats-summary">
              <div className="palette-stat-badge answered">
                <span>Answered:</span>
                <strong>{counts.answered}</strong>
              </div>
              <div className="palette-stat-badge not-answered">
                <span>Not Answered:</span>
                <strong>{counts.notAnswered}</strong>
              </div>
              <div className="palette-stat-badge marked">
                <span>Marked:</span>
                <strong>{counts.marked}</strong>
              </div>
              <div className="palette-stat-badge marked-answered">
                <span>Ans & Marked:</span>
                <strong>{counts.markedAnswered}</strong>
              </div>
            </div>
          </div>

          <div className="palette-body">
            {/* Question Number Buttons Grid */}
            <div className="palette-grid">
              {questions.map((q, idx) => {
                const status = getQuestionStatus(q.id);
                const isCurrent = idx === currentIndex;
                return (
                  <button
                    key={q.id}
                    type="button"
                    id={`palette-btn-${idx + 1}`}
                    className={`palette-item-btn status-${status} ${isCurrent ? 'is-current' : ''}`}
                    onClick={() => setCurrentIndex(idx)}
                    title={`Question ${idx + 1} (${status})`}
                  >
                    {idx + 1}
                  </button>
                );
              })}
            </div>

            {/* Status Color Legend */}
            <div className="palette-legend">
              <div className="legend-row">
                <span className="legend-chip answered" />
                <span>Answered</span>
              </div>
              <div className="legend-row">
                <span className="legend-chip not-answered" />
                <span>Not Answered</span>
              </div>
              <div className="legend-row">
                <span className="legend-chip marked" />
                <span>Marked for Review</span>
              </div>
              <div className="legend-row">
                <span className="legend-chip marked-answered" />
                <span>Answered & Marked for Review</span>
              </div>
              <div className="legend-row">
                <span className="legend-chip current" />
                <span>Current Question</span>
              </div>
            </div>
          </div>

          <div className="palette-footer">
            <button
              type="button"
              id="btn-submit-palette"
              className="btn btn-primary"
              style={{ width: '100%', justifyContent: 'center' }}
              disabled={isSubmitting || !!submitResult}
              onClick={() => setShowSubmitModal(true)}
            >
              <CheckCircle2 size={16} />
              Submit Test Series
            </button>
          </div>
        </aside>
      </div>

      {/* Mobile Palette Drawer & Backdrop */}
      <div
        className={`palette-drawer-overlay ${paletteDrawerOpen ? 'open' : ''}`}
        onClick={() => setPaletteDrawerOpen(false)}
        aria-hidden="true"
      >
        <div
          className="palette-drawer-content"
          onClick={(e) => e.stopPropagation()}
        >
          <div className="palette-header" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div className="palette-title" style={{ margin: 0 }}>Question Palette</div>
            <button
              type="button"
              className="drawer-close-btn"
              onClick={() => setPaletteDrawerOpen(false)}
              aria-label="Close palette drawer"
            >
              <X size={20} />
            </button>
          </div>

          <div className="palette-header" style={{ paddingTop: 0 }}>
            <div className="palette-stats-summary">
              <div className="palette-stat-badge answered">
                <span>Answered:</span>
                <strong>{counts.answered}</strong>
              </div>
              <div className="palette-stat-badge not-answered">
                <span>Not Answered:</span>
                <strong>{counts.notAnswered}</strong>
              </div>
              <div className="palette-stat-badge marked">
                <span>Marked:</span>
                <strong>{counts.marked}</strong>
              </div>
              <div className="palette-stat-badge marked-answered">
                <span>Ans & Marked:</span>
                <strong>{counts.markedAnswered}</strong>
              </div>
            </div>
          </div>

          <div className="palette-body">
            <div className="palette-grid">
              {questions.map((q, idx) => {
                const status = getQuestionStatus(q.id);
                const isCurrent = idx === currentIndex;
                return (
                  <button
                    key={q.id}
                    type="button"
                    className={`palette-item-btn status-${status} ${isCurrent ? 'is-current' : ''}`}
                    onClick={() => {
                      setCurrentIndex(idx);
                      setPaletteDrawerOpen(false);
                    }}
                  >
                    {idx + 1}
                  </button>
                );
              })}
            </div>

            <div className="palette-legend">
              <div className="legend-row">
                <span className="legend-chip answered" />
                <span>Answered</span>
              </div>
              <div className="legend-row">
                <span className="legend-chip not-answered" />
                <span>Not Answered</span>
              </div>
              <div className="legend-row">
                <span className="legend-chip marked" />
                <span>Marked for Review</span>
              </div>
              <div className="legend-row">
                <span className="legend-chip marked-answered" />
                <span>Answered & Marked for Review</span>
              </div>
              <div className="legend-row">
                <span className="legend-chip current" />
                <span>Current Question</span>
              </div>
            </div>
          </div>

          <div className="palette-footer">
            <button
              type="button"
              className="btn btn-primary"
              style={{ width: '100%', justifyContent: 'center' }}
              disabled={isSubmitting || !!submitResult}
              onClick={() => {
                setPaletteDrawerOpen(false);
                setShowSubmitModal(true);
              }}
            >
              <CheckCircle2 size={16} />
              Submit Test Series
            </button>
          </div>
        </div>
      </div>

      {/* Submit Confirmation Dialog */}
      {showSubmitModal && (
        <div className="confirm-modal-overlay">
          <div className="confirm-modal-card" role="dialog" aria-modal="true">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.75rem' }}>
              <ShieldAlert size={26} color="var(--teal-primary)" />
              <h3 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0 }}>
                Confirm Test Submission
              </h3>
            </div>

            <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', lineHeight: 1.5 }}>
              Are you sure you want to finish and submit your test? Once submitted, you cannot change any answers.
            </p>

            {/* Summary Statistics Counts */}
            <div className="confirm-stats-grid">
              <div className="confirm-stat-tile">
                <div className="num" style={{ color: 'var(--teal-primary)' }}>
                  {counts.total}
                </div>
                <div>
                  <div className="lbl">Total Questions</div>
                </div>
              </div>

              <div className="confirm-stat-tile">
                <div className="num" style={{ color: '#10b981' }}>
                  {counts.answered}
                </div>
                <div>
                  <div className="lbl">Answered</div>
                </div>
              </div>

              <div className="confirm-stat-tile">
                <div className="num" style={{ color: '#94a3b8' }}>
                  {counts.notAnswered}
                </div>
                <div>
                  <div className="lbl">Not Answered</div>
                </div>
              </div>

              <div className="confirm-stat-tile">
                <div className="num" style={{ color: '#c084fc' }}>
                  {counts.marked + counts.markedAnswered}
                </div>
                <div>
                  <div className="lbl">Marked for Review</div>
                </div>
              </div>
            </div>

            {/* Actions with double submit prevention */}
            <div className="confirm-modal-actions">
              <button
                type="button"
                id="btn-cancel-submit"
                className="btn btn-secondary"
                disabled={isSubmitting}
                onClick={() => setShowSubmitModal(false)}
              >
                Resume Test
              </button>

              <button
                type="button"
                id="btn-confirm-submit"
                className="btn btn-primary"
                disabled={isSubmitting}
                onClick={() => performSubmit(false)}
              >
                {isSubmitting ? (
                  <>
                    <span className="btn-spinner" />
                    Submitting Answers...
                  </>
                ) : (
                  <>
                    <CheckCircle2 size={16} />
                    Confirm & Submit
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Auto-Submitting Overlay (When timer hits 0) */}
      {autoSubmitting && !submitResult && (
        <div className="confirm-modal-overlay">
          <div className="confirm-modal-card" style={{ textAlign: 'center', maxWidth: 440 }}>
            <Clock size={48} color="#ef4444" style={{ margin: '0 auto 1.25rem' }} />
            <h3 style={{ fontSize: '1.3rem', fontWeight: 700, marginBottom: '0.5rem' }}>
              Time Has Expired!
            </h3>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
              Your examination time limit has finished. The system is automatically submitting your saved answers now...
            </p>
            <div className="btn-spinner" style={{ width: 32, height: 32, margin: '0 auto', borderWidth: 3 }} />
          </div>
        </div>
      )}

      {/* Post-Submission Result Scorecard Modal */}
      {submitResult && (
        <div className="confirm-modal-overlay">
          <div className="confirm-modal-card" style={{ maxWidth: 540 }}>
            <div className="scorecard-banner">
              <div className="scorecard-circle">
                <div className="score-main-val">
                  {Number(submitResult.marksObtained).toFixed(1)}
                </div>
                <div className="score-total-val">
                  / {Number(submitResult.totalMarks).toFixed(0)} Marks
                </div>
              </div>

              <h2 style={{ fontSize: '1.4rem', fontWeight: 700, margin: '0 0 0.35rem 0' }}>
                Test Submitted Successfully!
              </h2>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                {submitResult.testTitle}
              </p>
            </div>

            {/* Scorecard Grid Breakdown */}
            <div className="confirm-stats-grid">
              <div className="confirm-stat-tile">
                <div className="num" style={{ color: '#10b981' }}>
                  {submitResult.correct}
                </div>
                <div>
                  <div className="lbl">Correct Answers</div>
                </div>
              </div>

              <div className="confirm-stat-tile">
                <div className="num" style={{ color: '#ef4444' }}>
                  {submitResult.incorrect}
                </div>
                <div>
                  <div className="lbl">Incorrect Answers</div>
                </div>
              </div>

              <div className="confirm-stat-tile">
                <div className="num" style={{ color: '#94a3b8' }}>
                  {submitResult.unanswered}
                </div>
                <div>
                  <div className="lbl">Unanswered</div>
                </div>
              </div>

              <div className="confirm-stat-tile">
                <div className="num" style={{ color: 'var(--teal-primary)' }}>
                  {submitResult.percentage != null ? `${Number(submitResult.percentage).toFixed(1)}%` : '0%'}
                </div>
                <div>
                  <div className="lbl">Accuracy Score</div>
                </div>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'center', marginTop: '1.5rem', gap: '1rem' }}>
              <Link to="/tests" className="btn btn-primary" style={{ padding: '0.75rem 2rem' }}>
                <Award size={16} />
                Back to Available Tests
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
