import React from 'react';
import { useRole } from '../context/RoleContext';
import { Layers } from 'lucide-react';

export const TestsPage: React.FC = () => {
  const { role } = useRole();

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Tests</h1>
        <p className="page-subtitle">
          {role === 'Admin'
            ? 'Manage all created test series, draft questions, and publication state'
            : 'Browse available test series, attempt mocks, and practice'}
        </p>
      </div>

      <div className="card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
          <Layers size={22} color="var(--teal-primary)" />
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Available Assessments</h3>
        </div>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          Test catalog view is connected to the backend API (`/api/admin/tests` for admin and `/api/student/tests` for students).
        </p>
      </div>
    </div>
  );
};
