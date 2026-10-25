import React from 'react';
import { useRole } from '../context/RoleContext';
import { Award } from 'lucide-react';

export const ResultsPage: React.FC = () => {
  const { role } = useRole();

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Results & Analytics</h1>
        <p className="page-subtitle">
          {role === 'Admin'
            ? 'Overview of candidate performance, average scores, and question statistics'
            : 'Your test performance breakdown, scorecards, and detailed solutions'}
        </p>
      </div>

      <div className="card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
          <Award size={22} color="var(--teal-primary)" />
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Performance Reports</h3>
        </div>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          Solution views and PDF downloads will be subject to configured test access limits.
        </p>
      </div>
    </div>
  );
};
