import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { adminApi } from '../api/adminApi';
import { TestSeries } from '../types';
import {
  ArrowLeft,
  Upload,
  RefreshCw,
  CheckCircle,
  AlertCircle,
  Clock,
  Layers
} from 'lucide-react';

export const QuestionsReviewPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const testId = Number(id);

  const [test, setTest] = useState<TestSeries | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id || isNaN(testId)) {
      setError('Invalid test ID provided.');
      setLoading(false);
      return;
    }

    const fetchTest = async () => {
      try {
        setLoading(true);
        const data = await adminApi.getTestById(testId);
        setTest(data);
      } catch (err: any) {
        setError(err?.response?.message || err?.message || 'Failed to load test details.');
      } finally {
        setLoading(false);
      }
    };

    fetchTest();
  }, [id, testId]);

  if (loading) {
    return (
      <div style={{ padding: '3rem 0', textAlign: 'center', color: 'var(--text-secondary)' }}>
        <RefreshCw size={24} className="spin" style={{ margin: '0 auto 1rem', display: 'block', color: 'var(--teal-primary)' }} />
        <p>Loading questions review...</p>
      </div>
    );
  }

  if (error || !test) {
    return (
      <div>
        <div className="alert-box alert-error">
          <AlertCircle size={20} style={{ flexShrink: 0 }} />
          <span>{error || 'Test not found.'}</span>
        </div>
        <Link to="/tests" className="btn-secondary" style={{ textDecoration: 'none', display: 'inline-flex' }}>
          <ArrowLeft size={16} /> Back to Tests
        </Link>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '960px', margin: '0 auto' }}>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 className="page-title">Review Extracted Questions</h1>
          <p className="page-subtitle">
            Verify parsed questions, assign correct answers, and set marks before publishing.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to={`/tests/${testId}/upload`} className="btn-secondary" style={{ textDecoration: 'none' }}>
            <Upload size={15} /> Re-upload PDF
          </Link>
          <Link to="/tests" className="btn-secondary" style={{ textDecoration: 'none' }}>
            <ArrowLeft size={15} /> Back to Tests
          </Link>
        </div>
      </div>

      {/* Test details banner */}
      <div
        style={{
          backgroundColor: 'var(--bg-card)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-md)',
          padding: '1.25rem',
          marginBottom: '1.5rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.25rem' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>#{test.id}</span>
            <span style={{ fontWeight: 700, fontSize: '1.1rem', color: 'var(--text-primary)' }}>{test.title}</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
              <Clock size={13} color="var(--teal-primary)" /> {test.durationMinutes}m
            </span>
            <span>Status: <strong style={{ color: '#fbbf24' }}>{test.status}</strong></span>
            {test.sourcePdfPath && (
              <span style={{ color: 'var(--teal-hover)' }}>Source: {test.sourcePdfPath.split(/[\\/]/).pop()}</span>
            )}
          </div>
        </div>

        <div>
          <Link to={`/tests/${testId}/upload`} className="btn-sm btn-primary" style={{ textDecoration: 'none' }}>
            <Upload size={14} /> Upload / Re-parse PDF
          </Link>
        </div>
      </div>

      {/* Questions Placeholder / Review notice */}
      <div className="form-card" style={{ textAlign: 'center', padding: '3.5rem 2rem' }}>
        <div
          style={{
            width: '60px',
            height: '60px',
            borderRadius: '50%',
            backgroundColor: 'var(--teal-glow)',
            color: 'var(--teal-hover)',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: '1.25rem',
          }}
        >
          <Layers size={30} />
        </div>
        <h2 style={{ fontSize: '1.3rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
          Questions Extracted & Saved to Database
        </h2>
        <p style={{ maxWidth: '520px', margin: '0 auto 1.5rem', fontSize: '0.92rem', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
          Questions extracted from the PDF are saved in the database with <strong>correct_option = NULL</strong> and marks defaulting to <strong>4 / -1</strong>.
          The test remains in <strong>DRAFT</strong> status until you review and publish.
        </p>
        <div style={{ display: 'inline-flex', gap: '0.85rem' }}>
          <Link to={`/tests/${testId}/upload`} className="btn-secondary" style={{ textDecoration: 'none' }}>
            <Upload size={15} /> Upload PDF Page
          </Link>
          <Link to="/tests" className="btn-primary" style={{ textDecoration: 'none' }}>
            <CheckCircle size={15} /> Go to Tests Table
          </Link>
        </div>
      </div>
    </div>
  );
};
