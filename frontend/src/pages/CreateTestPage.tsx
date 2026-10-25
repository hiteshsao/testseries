import React from 'react';
import { UploadCloud } from 'lucide-react';

export const CreateTestPage: React.FC = () => {
  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Create Test</h1>
        <p className="page-subtitle">Upload PDF to extract questions or create assessment manually</p>
      </div>

      <div className="card" style={{ textAlign: 'center', padding: '3.5rem 1.5rem', borderStyle: 'dashed' }}>
        <div style={{
          width: 56,
          height: 56,
          margin: '0 auto 1.25rem',
          borderRadius: '50%',
          backgroundColor: 'var(--teal-glow)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'var(--teal-hover)'
        }}>
          <UploadCloud size={28} />
        </div>
        <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem', fontWeight: 600 }}>PDF Upload & Extraction Pipeline</h3>
        <p style={{ color: 'var(--text-secondary)', maxWidth: 520, margin: '0 auto 1.25rem', fontSize: '0.9rem' }}>
          Drag and drop your question paper PDF here. Supports text-based PDFs only.
        </p>
        <span className="badge-tag">OCR is not supported in this prototype</span>
      </div>
    </div>
  );
};
