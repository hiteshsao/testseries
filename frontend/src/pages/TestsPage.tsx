import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { adminApi } from '../api/adminApi';
import { TestSeries } from '../types';
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
  CheckCircle2,
  Clock,
  Layers
} from 'lucide-react';

export const TestsPage: React.FC = () => {
  const [tests, setTests] = useState<TestSeries[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'DRAFT' | 'PUBLISHED'>('ALL');
  const [actionFeedback, setActionFeedback] = useState<{ id: number; message: string; type: 'success' | 'error' } | null>(null);
  const [publishingId, setPublishingId] = useState<number | null>(null);

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

  const handlePublish = async (testId: number) => {
    setPublishingId(testId);
    setActionFeedback(null);
    try {
      const updated = await adminApi.publishTest(testId);
      setTests((prev) =>
        prev.map((t) => (t.id === testId ? updated : t))
      );
      setActionFeedback({
        id: testId,
        message: `Test "${updated.title}" is now PUBLISHED and live!`,
        type: 'success',
      });
    } catch (err: any) {
      setActionFeedback({
        id: testId,
        message: err?.response?.message || 'Failed to publish test.',
        type: 'error',
      });
    } finally {
      setPublishingId(null);
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
                        {/* 1. Upload PDF Action */}
                        <Link
                          to={`/create-test?step=upload&testId=${test.id}`}
                          className="btn-sm btn-action-ghost"
                          title="Upload question paper PDF"
                        >
                          <Upload size={13} />
                          <span>Upload PDF</span>
                        </Link>

                        {/* 2. Edit Questions Action */}
                        <Link
                          to={`/tests/${test.id}/questions`}
                          className="btn-sm btn-action-ghost"
                          title="Review & edit extracted questions"
                        >
                          <Edit3 size={13} />
                          <span>Edit Questions</span>
                        </Link>

                        {/* 3. Publish Action */}
                        {test.status === 'DRAFT' ? (
                          <button
                            type="button"
                            className="btn-sm btn-action-publish"
                            disabled={publishingId === test.id}
                            onClick={() => handlePublish(test.id)}
                            title="Publish this test series"
                          >
                            <CheckCircle size={13} />
                            <span>{publishingId === test.id ? 'Publishing...' : 'Publish'}</span>
                          </button>
                        ) : (
                          <span
                            className="btn-sm"
                            style={{ backgroundColor: 'rgba(16, 185, 129, 0.08)', color: '#34d399', cursor: 'default' }}
                            title="Test is already live"
                          >
                            <CheckCircle size={13} />
                            <span>Live</span>
                          </span>
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
    </div>
  );
};
