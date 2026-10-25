import React from 'react';
import { useRole } from '../context/RoleContext';
import { BookOpen, CheckCircle, Clock, FilePlus } from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { role } = useRole();

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Dashboard</h1>
        <p className="page-subtitle">
          Welcome to Crevolab Test Series ({role} View)
        </p>
      </div>

      <div className="grid-cards">
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Active Tests</span>
            <BookOpen size={20} color="var(--teal-primary)" />
          </div>
          <div className="stat-num">12</div>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.4rem' }}>
            Available for student attempts
          </p>
        </div>

        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Pending Drafts</span>
            <FilePlus size={20} color="var(--teal-primary)" />
          </div>
          <div className="stat-num">3</div>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.4rem' }}>
            Requires admin verification
          </p>
        </div>

        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Completed Attempts</span>
            <CheckCircle size={20} color="var(--teal-primary)" />
          </div>
          <div className="stat-num">148</div>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.4rem' }}>
            Across all test series
          </p>
        </div>

        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Avg. Duration</span>
            <Clock size={20} color="var(--teal-primary)" />
          </div>
          <div className="stat-num">65m</div>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.4rem' }}>
            Typical assessment duration
          </p>
        </div>
      </div>

      <div className="card" style={{ marginTop: '2rem' }}>
        <h2 style={{ fontSize: '1.15rem', marginBottom: '0.5rem', fontWeight: 600 }}>Workflow Quick Summary</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: '1.6' }}>
          PDF Upload &gt; Question Extraction &gt; Admin Review &gt; Publish &gt; Student Attempt &gt; Result
        </p>
        <div style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          <span className="badge-tag">PDFBox 3.x Ready</span>
          <span className="badge-tag">Text-based PDFs only</span>
          <span className="badge-tag">Simulated Payments</span>
        </div>
      </div>
    </div>
  );
};
