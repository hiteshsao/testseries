import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { adminApi, ApiError } from '../api/adminApi';
import { TestSeries } from '../types';
import { PublishProblemsModal } from '../components/PublishProblemsModal';
import {
  PlusCircle,
  RefreshCw,
  Upload,
  Edit3,
  CheckCircle,
  BarChart2,
  Search,
  Filter,
  FileQuestion,
  AlertCircle,
  AlertTriangle,
  CheckCircle2,
  Clock,
  FileText,
  Eye,
  X
} from 'lucide-react';

export const TestsPage: React.FC = () => {
  const [tests, setTests] = useState<TestSeries[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'DRAFT' | 'PUBLISHED'>('ALL');
  const [actionFeedback, setActionFeedback] = useState<{ id: number; message: string; type: 'success' | 'error' } | null>(null);
  const [publishingId, setPublishingId] = useState<number | null>(null);
  const [unpublishingId, setUnpublishingId] = useState<number | null>(null);

  // Publish failure problem modal state
  const [problemModal, setProblemModal] = useState<{
    isOpen: boolean;
    problems: string[];
    testTitle?: string;
    testId?: number;
  }>({
    isOpen: false,
    problems: [],
  });

  // DEMO-ONLY: Debug raw text modal state
  const [debugModal, setDebugModal] = useState<{
    isOpen: boolean;
    title: string;
    loading: boolean;
    text: string | null;
    error: string | null;
    meta?: { pages?: number; chars?: number };
  }>({
    isOpen: false,
    title: '',
    loading: false,
    text: null,
    error: null,
  });

  const handleViewRawText = async (testId: number, testTitle: string) => {
    setDebugModal({
      isOpen: true,
      title: testTitle,
      loading: true,
      text: null,
      error: null,
    });

    try {
      const res = await adminApi.getRawText(testId);
      setDebugModal({
        isOpen: true,
        title: testTitle,
        loading: false,
        text: res.rawText,
        error: null,
        meta: { pages: res.pageCount, chars: res.characterCount },
      });
    } catch (err: any) {
      setDebugModal({
        isOpen: true,
        title: testTitle,
        loading: false,
        text: null,
        error: err?.response?.message || err?.message || 'Failed to extract text from PDF.',
      });
    }
  };

  const fetchTests = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminApi.getTests();
      setTests(data);
    } catch (err: any) {
      setError(err?.response?.message || err?.message || 'Failed to load test series.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTests();
  }, []);

  const handlePublish = async (test: TestSeries) => {
    setPublishingId(test.id);
    setActionFeedback(null);
    try {
      const updated = await adminApi.publishTest(test.id);
      setTests((prev) =>
        prev.map((t) => (t.id === test.id ? updated : t))
      );
      setActionFeedback({
        id: test.id,
        message: `Test "${updated.title}" is now PUBLISHED and live!`,
        type: 'success',
      });
    } catch (err: any) {
      if (err instanceof ApiError && err.response?.problems && err.response.problems.length > 0) {
        setProblemModal({
          isOpen: true,
          problems: err.response.problems,
          testTitle: test.title,
          testId: test.id,
        });
      } else {
        const msg = err?.response?.message || err?.message || 'Failed to publish test.';
        setProblemModal({
          isOpen: true,
          problems: [msg],
          testTitle: test.title,
          testId: test.id,
        });
      }
    } finally {
      setPublishingId(null);
    }
  };

  const handleUnpublish = async (test: TestSeries) => {
    setUnpublishingId(test.id);
    setActionFeedback(null);
    try {
      const updated = await adminApi.unpublishTest(test.id);
      setTests((prev) =>
        prev.map((t) => (t.id === test.id ? updated : t))
      );
      setActionFeedback({
        id: test.id,
        message: `Test "${updated.title}" is now unpublished (DRAFT).`,
        type: 'success',
      });
    } catch (err: any) {
      setActionFeedback({
        id: test.id,
        message: err?.response?.message || 'Failed to unpublish test.',
        type: 'error',
      });
    } finally {
      setUnpublishingId(null);
    }
  };

  const filteredTests = tests.filter((t) => {
    const matchesSearch = t.title.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || t.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 className="page-title">Test Series Catalog</h1>
          <p className="page-subtitle">
            Manage assessment lifecycles, upload question PDFs, verify drafts, and publish
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="btn-secondary" onClick={fetchTests} disabled={loading}>
            <RefreshCw size={16} className={loading ? 'spin' : ''} />
            Refresh
          </button>
          <Link to="/create-test" className="btn-primary" style={{ textDecoration: 'none' }}>
            <PlusCircle size={18} />
            Create Test
          </Link>
        </div>
      </div>

      {actionFeedback && (
        <div className={`alert-box ${actionFeedback.type === 'success' ? 'alert-success' : 'alert-error'}`}>
          {actionFeedback.type === 'success' ? <CheckCircle2 size={20} /> : <AlertCircle size={20} />}
          <span>{actionFeedback.message}</span>
        </div>
      )}

      {error && (
        <div className="alert-box alert-error">
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {/* Filter and Search Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.25rem' }}>
        <div style={{ position: 'relative', minWidth: '280px', flex: '1', maxWidth: '420px' }}>
          <Search size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
          <input
            type="text"
            className="form-input"
            style={{ paddingLeft: '2.4rem' }}
            placeholder="Search test by title..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Filter size={16} style={{ color: 'var(--text-muted)' }} />
          <div className="segmented-control" style={{ width: 'auto' }}>
            <button
              className={`segmented-btn ${statusFilter === 'ALL' ? 'active' : ''}`}
              onClick={() => setStatusFilter('ALL')}
            >
              All ({tests.length})
            </button>
            <button
              className={`segmented-btn ${statusFilter === 'DRAFT' ? 'active' : ''}`}
              onClick={() => setStatusFilter('DRAFT')}
            >
              Drafts ({tests.filter(t => t.status === 'DRAFT').length})
            </button>
            <button
              className={`segmented-btn ${statusFilter === 'PUBLISHED' ? 'active' : ''}`}
              onClick={() => setStatusFilter('PUBLISHED')}
            >
              Published ({tests.filter(t => t.status === 'PUBLISHED').length})
            </button>
          </div>
        </div>
      </div>

      {/* Tests Table */}
      <div className="table-card">
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th style={{ width: '60px' }}>ID</th>
                <th>Test Title</th>
                <th>Duration</th>
                <th>Access & Price</th>
                <th>Limits</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '3rem' }}>
                    <div style={{ color: 'var(--teal-primary)', display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                      <RefreshCw size={20} className="spin" /> Loading tests...
                    </div>
                  </td>
                </tr>
              ) : filteredTests.length === 0 ? (
                <tr>
                  <td colSpan={7}>
                    <div className="empty-state">
                      <FileQuestion size={44} style={{ opacity: 0.4, margin: '0 auto 1rem', display: 'block' }} />
                      <p style={{ fontSize: '1.05rem', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.35rem' }}>
                        No test series match your filters
                      </p>
                      <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
                        Try searching with a different keyword or create a new test.
                      </p>
                      <Link to="/create-test" className="btn-primary" style={{ textDecoration: 'none', display: 'inline-flex' }}>
                        <PlusCircle size={16} /> Create Test Series
                      </Link>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredTests.map((test) => (
                  <tr key={test.id}>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.82rem', fontFamily: 'monospace' }}>
                      #{test.id}
                    </td>
                    <td>
                      <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                        {test.title}
                      </div>
                      {test.instructions && (
                        <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.2rem', maxWidth: '320px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {test.instructions}
                        </div>
                      )}
                      {test.sourcePdfPath && (
                        <div style={{ marginTop: '0.25rem' }}>
                          <span style={{ fontSize: '0.72rem', color: 'var(--teal-primary)', backgroundColor: 'var(--teal-glow)', padding: '2px 6px', borderRadius: '4px', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                            <Upload size={10} /> {test.sourcePdfPath}
                          </span>
                        </div>
                      )}
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--text-secondary)' }}>
                        <Clock size={14} color="var(--teal-primary)" />
                        <span>{test.durationMinutes}m</span>
                      </div>
                    </td>
                    <td>
                      <span className={`access-badge ${test.accessType === 'PAID' ? 'access-paid' : 'access-free'}`}>
                        {test.accessType === 'PAID' ? `₹${Number(test.price).toFixed(2)}` : 'FREE'}
                      </span>
                    </td>
                    <td style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                      <div>Attempts: <strong>{test.maxAttempts}</strong></div>
                      <div>Views: {test.maxSolutionViews} | DL: {test.maxPdfDownloads}</div>
                    </td>
                    <td>
                      <span className={`status-pill ${test.status === 'PUBLISHED' ? 'status-published' : 'status-draft'}`}>
                        {test.status === 'PUBLISHED' ? <CheckCircle size={12} /> : null}
                        {test.status}
                      </span>
                    </td>
                    <td>
                      <div className="action-buttons-group" style={{ justifyContent: 'flex-end' }}>
                        {/* 0. Preview Test Action */}
                        <Link
                          to={`/tests/${test.id}/preview`}
                          className="btn-sm btn-action-ghost"
                          title="Preview test in student-like read-only format"
                          id={`btn-preview-test-${test.id}`}
                        >
                          <Eye size={13} />
                          <span>Preview</span>
                        </Link>

                        {/* 1. Upload PDF Action */}
                        <Link
                          to={`/tests/${test.id}/upload`}
                          className="btn-sm btn-action-ghost"
                          title="Upload and extract questions from PDF"
                        >
                          <Upload size={13} />
                          <span>{test.sourcePdfPath ? 'Replace PDF' : 'Upload PDF'}</span>
                        </Link>

                        {/* DEMO-ONLY: Debug Raw Extracted Text */}
                        {test.sourcePdfPath && (
                          <button
                            type="button"
                            className="btn-sm btn-action-ghost"
                            onClick={() => handleViewRawText(test.id, test.title)}
                            title="[DEMO-ONLY] View raw extracted text from PDF"
                          >
                            <FileText size={13} />
                            <span>Raw Text</span>
                          </button>
                        )}

                        {/* 2. Edit Questions Action */}
                        <Link
                          to={`/tests/${test.id}/questions`}
                          className="btn-sm btn-action-ghost"
                          title="Review & edit extracted questions"
                        >
                          <Edit3 size={13} />
                          <span>Edit Questions</span>
                        </Link>

                        {/* 3. Publish / Unpublish Action */}
                        {test.status === 'DRAFT' ? (
                          <button
                            type="button"
                            className="btn-sm btn-action-publish"
                            disabled={publishingId === test.id}
                            onClick={() => handlePublish(test)}
                            title="Publish this test series"
                            id={`btn-publish-test-${test.id}`}
                          >
                            <CheckCircle size={13} />
                            <span>{publishingId === test.id ? 'Publishing...' : 'Publish'}</span>
                          </button>
                        ) : (
                          <button
                            type="button"
                            className="btn-sm btn-action-ghost"
                            style={{ color: '#f87171', borderColor: 'rgba(239, 68, 68, 0.3)' }}
                            disabled={unpublishingId === test.id}
                            onClick={() => handleUnpublish(test)}
                            title="Unpublish this test back to DRAFT"
                            id={`btn-unpublish-test-${test.id}`}
                          >
                            <AlertTriangle size={13} />
                            <span>{unpublishingId === test.id ? 'Unpublishing...' : 'Unpublish'}</span>
                          </button>
                        )}

                        {/* 4. Results Action */}
                        <Link
                          to={`/results?testId=${test.id}`}
                          className="btn-sm btn-action-ghost"
                          title="View student attempts & performance"
                        >
                          <BarChart2 size={13} />
                          <span>Results</span>
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>


      {/* DEMO-ONLY: Raw Extracted Text Modal */}
      {debugModal.isOpen && (
        <div style={{
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
        }}>
          <div style={{
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-lg)',
            width: '100%',
            maxWidth: '850px',
            maxHeight: '85vh',
            display: 'flex',
            flexDirection: 'column',
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.5)',
          }}>
            {/* Modal Header */}
            <div style={{
              padding: '1.25rem 1.5rem',
              borderBottom: '1px solid var(--border-subtle)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-primary)' }}>
                    Raw Extracted PDF Text
                  </h3>
                  <span className="badge-tag" style={{ fontSize: '0.7rem', padding: '1px 6px' }}>DEMO-ONLY</span>
                </div>
                <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                  Test: {debugModal.title}
                </p>
              </div>
              <button
                type="button"
                className="btn-action-ghost"
                style={{ padding: '6px', borderRadius: '50%' }}
                onClick={() => setDebugModal((prev) => ({ ...prev, isOpen: false }))}
              >
                <X size={18} />
              </button>
            </div>

            {/* Modal Body */}
            <div style={{ padding: '1.5rem', overflowY: 'auto', flex: 1 }}>
              {debugModal.loading && (
                <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--text-muted)' }}>
                  <RefreshCw size={24} className="spin" style={{ marginBottom: '0.5rem' }} />
                  <p>Extracting text from PDF using PDFBox TextStripper...</p>
                </div>
              )}

              {debugModal.error && (
                <div className="alert-box alert-error">
                  <AlertCircle size={22} style={{ flexShrink: 0 }} />
                  <div>
                    <strong>Extraction Error:</strong>
                    <p style={{ marginTop: '0.25rem' }}>{debugModal.error}</p>
                  </div>
                </div>
              )}

              {debugModal.text && (
                <div>
                  <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
                    {debugModal.meta?.pages != null && (
                      <span className="badge-tag">
                        Pages: <strong>{debugModal.meta.pages}</strong>
                      </span>
                    )}
                    {debugModal.meta?.chars != null && (
                      <span className="badge-tag">
                        Characters: <strong>{debugModal.meta.chars}</strong>
                      </span>
                    )}
                    <span className="badge-tag" style={{ color: 'var(--teal-primary)' }}>
                      Sorted by Position: Enabled
                    </span>
                  </div>

                  <pre style={{
                    backgroundColor: 'var(--bg-input)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-md)',
                    padding: '1.25rem',
                    color: 'var(--text-primary)',
                    fontFamily: 'monospace',
                    fontSize: '0.85rem',
                    lineHeight: '1.6',
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-word',
                    maxHeight: '450px',
                    overflowY: 'auto',
                  }}>
                    {debugModal.text}
                  </pre>
                </div>
              )}
            </div>

            {/* Modal Footer */}
            <div style={{
              padding: '1rem 1.5rem',
              borderTop: '1px solid var(--border-subtle)',
              display: 'flex',
              justifyContent: 'flex-end',
            }}>
              <button
                type="button"
                className="btn-secondary"
                onClick={() => setDebugModal((prev) => ({ ...prev, isOpen: false }))}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Publish Failure Problems Modal */}
      <PublishProblemsModal
        isOpen={problemModal.isOpen}
        onClose={() => setProblemModal((prev) => ({ ...prev, isOpen: false }))}
        problems={problemModal.problems}
        testTitle={problemModal.testTitle}
        testId={problemModal.testId}
      />
    </div>
  );
};
