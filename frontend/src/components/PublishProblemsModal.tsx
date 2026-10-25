import React from 'react';
import { Link } from 'react-router-dom';
import { AlertTriangle, X, ArrowRight, ShieldAlert, Edit3 } from 'lucide-react';

interface PublishProblemsModalProps {
  isOpen: boolean;
  onClose: () => void;
  problems: string[];
  testTitle?: string;
  testId?: number;
}

export const PublishProblemsModal: React.FC<PublishProblemsModalProps> = ({
  isOpen,
  onClose,
  problems,
  testTitle,
  testId,
}) => {
  if (!isOpen) return null;

  const hasQuestionProblems = problems.some(
    (p) => p.toLowerCase().includes('question') || p.toLowerCase().includes('correct answer')
  );

  return (
    <div
      id="publish-problems-modal-backdrop"
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.78)',
        backdropFilter: 'blur(5px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1100,
        padding: '1rem',
      }}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        id="publish-problems-modal"
        style={{
          backgroundColor: 'var(--bg-card)',
          border: '1px solid rgba(239, 68, 68, 0.4)',
          borderRadius: 'var(--radius-lg)',
          width: '100%',
          maxWidth: '620px',
          maxHeight: '90vh',
          display: 'flex',
          flexDirection: 'column',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.7), 0 0 25px rgba(239, 68, 68, 0.2)',
          overflow: 'hidden',
          animation: 'fadeIn 0.2s ease-out',
        }}
      >
        {/* Modal Header */}
        <div
          style={{
            padding: '1.25rem 1.5rem',
            borderBottom: '1px solid var(--border-subtle)',
            backgroundColor: 'rgba(239, 68, 68, 0.08)',
            display: 'flex',
            alignItems: 'flex-start',
            justifyContent: 'space-between',
            gap: '1rem',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
            <div
              style={{
                width: '42px',
                height: '42px',
                borderRadius: '50%',
                backgroundColor: 'rgba(239, 68, 68, 0.15)',
                color: '#ef4444',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              <ShieldAlert size={24} />
            </div>
            <div>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                Publish Validation Failed
              </h3>
              <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: '0.15rem' }}>
                {testTitle ? `"${testTitle}"` : 'This test'} cannot be published until all issues are resolved.
              </p>
            </div>
          </div>
          <button
            id="close-publish-problems-modal-btn"
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--text-muted)',
              cursor: 'pointer',
              padding: '0.25rem',
              borderRadius: 'var(--radius-sm)',
            }}
            aria-label="Close modal"
          >
            <X size={18} />
          </button>
        </div>

        {/* Modal Body: Problems List */}
        <div style={{ padding: '1.5rem', overflowY: 'auto', flex: 1 }}>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              marginBottom: '1rem',
            }}
          >
            <span style={{ fontSize: '0.88rem', fontWeight: 600, color: 'var(--text-primary)' }}>
              Problems to Fix ({problems.length})
            </span>
            <span
              style={{
                fontSize: '0.75rem',
                backgroundColor: 'rgba(239, 68, 68, 0.15)',
                color: '#f87171',
                padding: '2px 8px',
                borderRadius: '999px',
                fontWeight: 600,
              }}
            >
              HTTP 400 Bad Request
            </span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }} id="publish-problems-list">
            {problems.map((problem, index) => (
              <div
                key={index}
                className="problem-item"
                style={{
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '0.75rem',
                  padding: '0.85rem 1rem',
                  backgroundColor: 'rgba(239, 68, 68, 0.06)',
                  border: '1px solid rgba(239, 68, 68, 0.25)',
                  borderRadius: 'var(--radius-md)',
                }}
              >
                <AlertTriangle size={16} color="#ef4444" style={{ marginTop: '0.2rem', flexShrink: 0 }} />
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: '0.88rem', color: '#fecaca', fontWeight: 500 }}>
                    {problem}
                  </div>
                  {problem.toLowerCase().includes('correct answer') && (
                    <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                      Tip: Open Questions Review and select the correct option (A, B, C, or D).
                    </div>
                  )}
                  {problem.toLowerCase().includes('at least one question') && (
                    <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                      Tip: Upload a PDF to extract questions or add questions manually.
                    </div>
                  )}
                  {problem.toLowerCase().includes('duration') && (
                    <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                      Tip: Test duration must be at least 1 minute.
                    </div>
                  )}
                  {problem.toLowerCase().includes('price') && (
                    <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                      Tip: For a PAID test, set a price greater than ₹0.00.
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

          <div
            style={{
              marginTop: '1.25rem',
              padding: '0.85rem 1rem',
              backgroundColor: 'var(--bg-input)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-md)',
              fontSize: '0.8rem',
              color: 'var(--text-secondary)',
            }}
          >
            <strong>Note:</strong> Publishing publishes this test series to the student portal. To protect candidate experience, all questions must have answer keys and valid exam parameters configured.
          </div>
        </div>

        {/* Modal Footer */}
        <div
          style={{
            padding: '1rem 1.5rem',
            borderTop: '1px solid var(--border-subtle)',
            backgroundColor: 'rgba(0, 0, 0, 0.2)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '1rem',
          }}
        >
          <button
            type="button"
            className="btn-secondary"
            onClick={onClose}
            id="close-publish-problems-btn"
          >
            Close
          </button>

          {testId && hasQuestionProblems && (
            <Link
              to={`/tests/${testId}/questions`}
              className="btn-primary"
              style={{ textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}
              onClick={onClose}
              id="fix-in-questions-review-btn"
            >
              <Edit3 size={15} />
              <span>Review Questions Now</span>
              <ArrowRight size={14} />
            </Link>
          )}
        </div>
      </div>
    </div>
  );
};
