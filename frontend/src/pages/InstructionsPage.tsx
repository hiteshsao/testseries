import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { studentApi } from '../api/studentApi';
import { StudentTestSummary } from '../types';
import {
  Clock,
  Award,
  FileQuestion,
  CheckCircle2,
  AlertCircle,
  ArrowLeft,
  Play,
  ShieldCheck,
  Bookmark,
  Check
} from 'lucide-react';

export const InstructionsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const testId = Number(id);
  const navigate = useNavigate();

  const [test, setTest] = useState<StudentTestSummary | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [declared, setDeclared] = useState<boolean>(false);
  const [starting, setStarting] = useState<boolean>(false);

  useEffect(() => {
    const fetchTestDetails = async () => {
      if (!testId) {
        setError('Invalid Test ID.');
        setLoading(false);
        return;
      }

      setLoading(true);
      setError(null);
      try {
        const data = await studentApi.getPublishedTestById(testId);
        setTest(data);
      } catch (err: any) {
        setError(err?.response?.message || err?.message || 'Failed to load test details.');
      } finally {
        setLoading(false);
      }
    };

    fetchTestDetails();
  }, [testId]);

  const handleStartTest = async () => {
    if (!test || !declared || starting) return;

    setStarting(true);
    try {
      const attempt = await studentApi.startTest(test.id);
      navigate(`/tests/${test.id}/attempt/${attempt.attemptId}`);
    } catch (err: any) {
      alert(err?.response?.message || err?.message || 'Failed to start test attempt.');
      setStarting(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '4rem 1rem' }}>
        <div className="btn-spinner" style={{ width: 36, height: 36, margin: '0 auto 1.5rem', borderWidth: 3 }} />
        <h2 style={{ fontSize: '1.25rem', color: 'var(--text-primary)' }}>Loading Test Instructions...</h2>
        <p style={{ color: 'var(--text-secondary)', marginTop: '0.5rem' }}>Fetching test metadata and rules from server.</p>
      </div>
    );
  }

  if (error || !test) {
    return (
      <div className="instructions-container" style={{ paddingTop: '2rem' }}>
        <div className="instruction-rules-card" style={{ borderColor: 'rgba(239, 68, 68, 0.4)', textAlign: 'center' }}>
          <AlertCircle size={44} color="#ef4444" style={{ margin: '0 auto 1rem' }} />
          <h2 style={{ fontSize: '1.3rem', color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
            Unable to Load Test
          </h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
            {error || 'The requested test could not be found or is not currently published.'}
          </p>
          <Link to="/tests" className="btn btn-secondary">
            <ArrowLeft size={16} />
            Back to Tests List
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="instructions-container">
      {/* Top Breadcrumb & Title */}
      <div style={{ marginBottom: '1.5rem' }}>
        <Link
          to="/tests"
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.45rem',
            color: 'var(--text-secondary)',
            textDecoration: 'none',
            fontSize: '0.88rem',
            marginBottom: '0.75rem',
            transition: 'color var(--transition-fast)',
          }}
          onMouseEnter={(e) => ((e.target as HTMLElement).style.color = 'var(--teal-primary)')}
          onMouseLeave={(e) => ((e.target as HTMLElement).style.color = 'var(--text-secondary)')}
        >
          <ArrowLeft size={15} />
          Back to Tests List
        </Link>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
          <h1 className="page-title" style={{ margin: 0 }}>
            {test.title}
          </h1>
          <span className="badge badge-published" style={{ textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            CBT Online Test
          </span>
          {test.accessType === 'FREE' ? (
            <span className="badge" style={{ backgroundColor: 'rgba(16, 185, 129, 0.15)', color: '#10b981', border: '1px solid rgba(16, 185, 129, 0.3)' }}>
              FREE
            </span>
          ) : (
            <span className="badge" style={{ backgroundColor: 'rgba(245, 158, 11, 0.15)', color: '#f59e0b', border: '1px solid rgba(245, 158, 11, 0.3)' }}>
              PAID (₹{test.price})
            </span>
          )}
        </div>
        <p className="page-subtitle" style={{ marginTop: '0.35rem' }}>
          Please carefully review all guidelines and marking scheme before initiating your test attempt.
        </p>
      </div>

      {/* Test Key Metadata Grid */}
      <div className="instruction-meta-grid">
        <div className="instruction-stat-box">
          <div className="instruction-stat-icon">
            <Clock size={22} />
          </div>
          <div>
            <div className="instruction-stat-val">{test.durationMinutes} Mins</div>
            <div className="instruction-stat-lbl">Duration</div>
          </div>
        </div>

        <div className="instruction-stat-box">
          <div className="instruction-stat-icon">
            <FileQuestion size={22} />
          </div>
          <div>
            <div className="instruction-stat-val">{test.totalQuestions ?? '—'}</div>
            <div className="instruction-stat-lbl">Questions</div>
          </div>
        </div>

        <div className="instruction-stat-box">
          <div className="instruction-stat-icon">
            <Award size={22} />
          </div>
          <div>
            <div className="instruction-stat-val">{test.totalMarks != null ? Number(test.totalMarks).toFixed(0) : '—'}</div>
            <div className="instruction-stat-lbl">Total Marks</div>
          </div>
        </div>

        <div className="instruction-stat-box">
          <div className="instruction-stat-icon" style={{ backgroundColor: 'rgba(99, 102, 241, 0.15)', color: '#818cf8' }}>
            <ShieldCheck size={22} />
          </div>
          <div>
            <div className="instruction-stat-val">
              {test.maxAttempts ? `${test.attemptsUsed || 0} / ${test.maxAttempts}` : 'Unlimited'}
            </div>
            <div className="instruction-stat-lbl">Attempts Used</div>
          </div>
        </div>
      </div>

      {/* Specific Test Instructions (from author/admin) */}
      {test.instructions && (
        <div className="instruction-rules-card">
          <h3>
            <CheckCircle2 size={18} color="var(--teal-primary)" />
            Test-Specific Instructions
          </h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', lineHeight: 1.6, whiteSpace: 'pre-wrap' }}>
            {test.instructions}
          </p>
        </div>
      )}

      {/* General CBT Guidelines & Palette Legend */}
      <div className="instruction-rules-card">
        <h3>
          <ShieldCheck size={18} color="var(--teal-primary)" />
          CBT Examination Guidelines
        </h3>

        <ul className="instruction-rules-list">
          <li>
            <span className="instruction-bullet-num">1</span>
            <div>
              <strong>Server-Synchronized Timer:</strong> The countdown timer at the top is driven by the server ends-at clock.
              The clock will continue running even if you refresh or switch windows.
            </div>
          </li>
          <li>
            <span className="instruction-bullet-num">2</span>
            <div>
              <strong>Instant Autosave:</strong> Each answer choice and review flag you select is immediately saved to the server via the API.
              You do not need to manually save your individual responses.
            </div>
          </li>
          <li>
            <span className="instruction-bullet-num">3</span>
            <div>
              <strong>Auto-Submit on Time Expiry:</strong> When the countdown timer reaches 00:00:00, your test will be
              automatically submitted by the system without requiring confirmation.
            </div>
          </li>
          <li>
            <span className="instruction-bullet-num">4</span>
            <div>
              <strong>Single Final Submission:</strong> Once you submit the test, you cannot edit or re-attempt this session.
              Double submissions are strictly locked.
            </div>
          </li>
        </ul>

        {/* Question Palette Color Explanation */}
        <h4 style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-primary)', marginTop: '1.5rem', marginBottom: '0.75rem' }}>
          Question Palette Color Code Reference:
        </h4>

        <div className="palette-demo-grid">
          <div className="palette-demo-item">
            <span style={{ width: 18, height: 18, borderRadius: 4, backgroundColor: '#10b981', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
              <Check size={12} color="#fff" />
            </span>
            <span><strong>Answered:</strong> Option saved</span>
          </div>

          <div className="palette-demo-item">
            <span style={{ width: 18, height: 18, borderRadius: 4, backgroundColor: '#475569', display: 'inline-block' }} />
            <span><strong>Not Answered:</strong> Yet to attempt</span>
          </div>

          <div className="palette-demo-item">
            <span style={{ width: 18, height: 18, borderRadius: 4, backgroundColor: '#a855f7', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
              <Bookmark size={11} color="#fff" />
            </span>
            <span><strong>Marked for Review:</strong> Flagged</span>
          </div>

          <div className="palette-demo-item">
            <span style={{ width: 18, height: 18, borderRadius: 4, backgroundColor: '#8b5cf6', position: 'relative', display: 'inline-block' }}>
              <span style={{ position: 'absolute', top: 2, right: 2, width: 5, height: 5, borderRadius: '50%', backgroundColor: '#10b981' }} />
            </span>
            <span><strong>Answered & Marked:</strong> Evaluated</span>
          </div>
        </div>
      </div>

      {/* Declaration Checkbox */}
      <label className="declaration-box">
        <input
          type="checkbox"
          className="declaration-checkbox"
          id="declaration-checkbox"
          checked={declared}
          onChange={(e) => setDeclared(e.target.checked)}
        />
        <div style={{ fontSize: '0.92rem', color: 'var(--text-primary)', lineHeight: 1.45 }}>
          <strong>Candidate Declaration:</strong> I have read and understood all the instructions and rules governing this examination.
          I understand that closing or refreshing the tab will not pause the timer. I am ready to begin the test.
        </div>
      </label>

      {/* Action Buttons */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
        <Link to="/tests" className="btn btn-secondary">
          <ArrowLeft size={16} />
          Back to Tests
        </Link>

        <button
          type="button"
          id="btn-start-test"
          className="btn btn-primary"
          style={{ padding: '0.85rem 2rem', fontSize: '1rem', fontWeight: 600 }}
          disabled={!declared || starting || !test.canAttempt}
          onClick={handleStartTest}
        >
          {starting ? (
            <>
              <span className="btn-spinner" />
              Initializing Exam...
            </>
          ) : !test.canAttempt ? (
            'Max Attempts Limit Reached'
          ) : (
            <>
              <Play size={18} />
              Start Test Now
            </>
          )}
        </button>
      </div>
    </div>
  );
};
