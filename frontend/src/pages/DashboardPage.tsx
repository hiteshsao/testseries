import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useRole } from '../context/RoleContext';
import { adminApi } from '../api/adminApi';
import { AdminStats, TestSeries } from '../types';
import {
  BookOpen,
  CheckCircle2,
  FileEdit,
  Users,
  RefreshCw,
  PlusCircle,
  ArrowRight,
  AlertCircle,
  FileText
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { role } = useRole();
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [recentTests, setRecentTests] = useState<TestSeries[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [statsData, testsData] = await Promise.all([
        adminApi.getStats(),
        adminApi.getTests(),
      ]);
      setStats(statsData);
      setRecentTests(testsData.slice(0, 5));
    } catch (err: any) {
      console.error('Failed to fetch dashboard data', err);
      setError(err?.response?.message || err?.message || 'Failed to connect to backend API');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 className="page-title">Admin Dashboard</h1>
          <p className="page-subtitle">
            Overview of assessment catalog, publication pipeline, and attempt metrics ({role} mode)
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button
            className="btn-secondary"
            onClick={fetchDashboardData}
            disabled={loading}
            title="Refresh statistics"
          >
            <RefreshCw size={16} className={loading ? 'spin' : ''} />
            Refresh
          </button>
          <Link to="/create-test" className="btn-primary" style={{ textDecoration: 'none' }}>
            <PlusCircle size={18} />
            Create Test
          </Link>
        </div>
      </div>

      {error && (
        <div className="alert-box alert-error">
          <AlertCircle size={20} style={{ flexShrink: 0 }} />
          <div>
            <strong>API Connection Issue: </strong>
            <span>{error}</span>
            <div style={{ marginTop: '0.35rem', fontSize: '0.82rem' }}>
              Make sure backend is running on port 8080.
            </div>
          </div>
        </div>
      )}

      {/* Real Stat Cards */}
      <div className="grid-cards">
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', fontWeight: 600 }}>Total Tests</span>
            <BookOpen size={20} color="var(--teal-primary)" />
          </div>
          <div className="stat-num">
            {loading ? '...' : (stats?.totalTests ?? 0)}
          </div>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.4rem' }}>
            All created assessments
          </p>
        </div>

        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', fontWeight: 600 }}>Published</span>
            <CheckCircle2 size={20} color="#34d399" />
          </div>
          <div className="stat-num" style={{ color: '#34d399' }}>
            {loading ? '...' : (stats?.publishedTests ?? 0)}
          </div>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.4rem' }}>
            Live for student attempts
          </p>
        </div>

        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', fontWeight: 600 }}>Pending Drafts</span>
            <FileEdit size={20} color="#fbbf24" />
          </div>
          <div className="stat-num" style={{ color: '#fbbf24' }}>
            {loading ? '...' : (stats?.draftTests ?? 0)}
          </div>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.4rem' }}>
            Pending review & publication
          </p>
        </div>

        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', fontWeight: 600 }}>Total Attempts</span>
            <Users size={20} color="#60a5fa" />
          </div>
          <div className="stat-num" style={{ color: '#60a5fa' }}>
            {loading ? '...' : (stats?.totalAttempts ?? 0)}
          </div>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.4rem' }}>
            Student submissions recorded
          </p>
        </div>
      </div>

      {/* Recent Tests Section */}
      <div style={{ marginTop: '2.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-primary)' }}>
            Recent Test Series
          </h2>
          <Link to="/tests" style={{ color: 'var(--teal-primary)', textDecoration: 'none', fontSize: '0.88rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
            View All Tests <ArrowRight size={16} />
          </Link>
        </div>

        <div className="table-card">
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Duration</th>
                  <th>Access</th>
                  <th>Status</th>
                  <th>Quick Actions</th>
                </tr>
              </thead>
              <tbody>
                {recentTests.length === 0 ? (
                  <tr>
                    <td colSpan={5}>
                      <div className="empty-state">
                        <FileText size={40} style={{ opacity: 0.4, margin: '0 auto 1rem', display: 'block' }} />
                        <p style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.35rem' }}>
                          No tests found in database
                        </p>
                        <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
                          Get started by creating your first assessment.
                        </p>
                        <Link to="/create-test" className="btn-primary" style={{ textDecoration: 'none', display: 'inline-flex' }}>
                          <PlusCircle size={16} /> Create Test
                        </Link>
                      </div>
                    </td>
                  </tr>
                ) : (
                  recentTests.map((test) => (
                    <tr key={test.id}>
                      <td style={{ fontWeight: 600 }}>{test.title}</td>
                      <td>{test.durationMinutes} mins</td>
                      <td>
                        <span className={`access-badge ${test.accessType === 'PAID' ? 'access-paid' : 'access-free'}`}>
                          {test.accessType === 'PAID' ? `₹${test.price}` : 'FREE'}
                        </span>
                      </td>
                      <td>
                        <span className={`status-pill ${test.status === 'PUBLISHED' ? 'status-published' : 'status-draft'}`}>
                          {test.status}
                        </span>
                      </td>
                      <td>
                        <div className="action-buttons-group">
                          <Link to="/tests" className="btn-sm btn-action-ghost">
                            Manage
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
    </div>
  );
};
