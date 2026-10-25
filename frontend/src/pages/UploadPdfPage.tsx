import React, { useEffect, useState, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import { adminApi } from '../api/adminApi';
import { TestSeries, ParseResult } from '../types';
import {
  Upload,
  FileText,
  AlertTriangle,
  AlertCircle,
  CheckCircle2,
  ArrowLeft,
  X,
  FileQuestion,
  Info,
  Clock,
  Sparkles,
  RefreshCw,
  Edit3
} from 'lucide-react';

export const UploadPdfPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const testId = Number(id);

  const [test, setTest] = useState<TestSeries | null>(null);
  const [loadingTest, setLoadingTest] = useState<boolean>(true);
  const [testError, setTestError] = useState<string | null>(null);

  // File selection & drag-and-drop state
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragOver, setIsDragOver] = useState<boolean>(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  // Processing state
  type Step = 'idle' | 'uploading' | 'parsing' | 'success' | 'error';
  const [step, setStep] = useState<Step>('idle');
  const [progressPercent, setProgressPercent] = useState<number>(0);
  const [statusMessage, setStatusMessage] = useState<string>('');

  // Outcome state
  const [parseResult, setParseResult] = useState<ParseResult | null>(null);
  const [scannedPdfError, setScannedPdfError] = useState<string | null>(null);
  const [generalError, setGeneralError] = useState<string | null>(null);

  useEffect(() => {
    if (!id || isNaN(testId)) {
      setTestError('Invalid test ID provided.');
      setLoadingTest(false);
      return;
    }

    const fetchTest = async () => {
      try {
        setLoadingTest(true);
        const data = await adminApi.getTestById(testId);
        setTest(data);
      } catch (err: any) {
        setTestError(err?.response?.message || err?.message || 'Could not load test details.');
      } finally {
        setLoadingTest(false);
      }
    };

    fetchTest();
  }, [id, testId]);

  // Format file size nicely
  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  // Validate and set file
  const handleFileSelection = (file: File) => {
    setScannedPdfError(null);
    setGeneralError(null);
    setParseResult(null);
    setStep('idle');

    if (!file.name.toLowerCase().endsWith('.pdf') && file.type !== 'application/pdf') {
      setGeneralError('Invalid file type. Please upload a valid PDF document (.pdf).');
      setSelectedFile(null);
      return;
    }

    // 10 MB limit check
    const maxSizeBytes = 10 * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      setGeneralError('File exceeds the 10 MB limit. Please select a smaller PDF file.');
      setSelectedFile(null);
      return;
    }

    setSelectedFile(file);
  };

  // Drag-and-drop handlers
  const handleDragEnter = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(true);
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(true);
  };

  const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFileSelection(e.dataTransfer.files[0]);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      handleFileSelection(e.target.files[0]);
    }
  };

  const handleClearFile = (e?: React.MouseEvent) => {
    if (e) e.stopPropagation();
    setSelectedFile(null);
    setParseResult(null);
    setScannedPdfError(null);
    setGeneralError(null);
    setStep('idle');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  // Main upload + parse flow
  const handleUploadAndParse = async () => {
    if (!selectedFile || !testId) return;

    setScannedPdfError(null);
    setGeneralError(null);
    setParseResult(null);

    try {
      // Step 1: Uploading PDF
      setStep('uploading');
      setProgressPercent(35);
      setStatusMessage('Uploading PDF to secure server...');

      const updatedTest = await adminApi.uploadPdf(testId, selectedFile);
      setTest(updatedTest);

      // Step 2: Parsing Questions
      setStep('parsing');
      setProgressPercent(75);
      setStatusMessage('Extracting text with PDFBox and parsing questions & options...');

      const result = await adminApi.parseQuestions(testId);

      // Step 3: Success
      setProgressPercent(100);
      setStatusMessage('Parsing completed successfully!');
      setParseResult(result);
      setStep('success');
    } catch (err: any) {
      setStep('error');
      setProgressPercent(0);

      const errCode = err?.response?.errorCode || err?.response?.code || '';
      const errMsg = err?.response?.message || err?.message || 'An error occurred during upload or parsing.';

      if (errCode === 'SCANNED_PDF_NOT_SUPPORTED' || errMsg.includes('scanned PDF') || errMsg.includes('SCANNED_PDF_NOT_SUPPORTED')) {
        setScannedPdfError(
          'This looks like a scanned PDF. OCR is not supported in this prototype. Please upload a text-based PDF.'
        );
      } else {
        setGeneralError(errMsg);
      }
    }
  };

  if (loadingTest) {
    return (
      <div style={{ padding: '3rem 0', textAlign: 'center', color: 'var(--text-secondary)' }}>
        <RefreshCw size={24} className="spin" style={{ margin: '0 auto 1rem', display: 'block', color: 'var(--teal-primary)' }} />
        <p>Loading test details...</p>
      </div>
    );
  }

  if (testError || !test) {
    return (
      <div>
        <div className="alert-box alert-error" style={{ marginTop: '1rem' }}>
          <AlertCircle size={20} style={{ flexShrink: 0 }} />
          <div>
            <strong>Error: </strong>
            <span>{testError || 'Test not found.'}</span>
          </div>
        </div>
        <Link to="/tests" className="btn-secondary" style={{ textDecoration: 'none', display: 'inline-flex' }}>
          <ArrowLeft size={16} /> Back to Tests
        </Link>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '860px', margin: '0 auto' }}>
      {/* Top Header */}
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 className="page-title">Upload Question Paper PDF</h1>
          <p className="page-subtitle">
            Upload a text-based PDF to automatically extract questions and options for this test series.
          </p>
        </div>
        <Link to="/tests" className="btn-secondary" style={{ textDecoration: 'none' }}>
          <ArrowLeft size={16} /> Back to Tests
        </Link>
      </div>

      {/* Test Metadata Overview Bar */}
      <div
        style={{
          backgroundColor: 'var(--bg-card)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-md)',
          padding: '1rem 1.25rem',
          marginBottom: '1.5rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.2rem' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
              #{test.id}
            </span>
            <span style={{ fontWeight: 700, fontSize: '1.05rem', color: 'var(--text-primary)' }}>
              {test.title}
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
              <Clock size={13} color="var(--teal-primary)" /> {test.durationMinutes} minutes
            </span>
            <span>
              Access: <strong>{test.accessType === 'PAID' ? `₹${Number(test.price).toFixed(2)}` : 'FREE'}</strong>
            </span>
            {test.sourcePdfPath && (
              <span style={{ color: 'var(--teal-hover)' }}>
                PDF: {test.sourcePdfPath.split(/[\\/]/).pop()}
              </span>
            )}
          </div>
        </div>
        <div>
          <span className={`status-pill ${test.status === 'PUBLISHED' ? 'status-published' : 'status-draft'}`}>
            {test.status}
          </span>
        </div>
      </div>

      {/* Prominent Required Note: Questions Not Published Until Reviewed */}
      <div
        className="alert-box alert-info"
        style={{
          display: 'flex',
          alignItems: 'flex-start',
          gap: '0.85rem',
          boxShadow: '0 2px 10px rgba(59, 130, 246, 0.08)',
        }}
      >
        <Info size={20} color="#60a5fa" style={{ flexShrink: 0, marginTop: '2px' }} />
        <div style={{ flex: 1, fontSize: '0.92rem', lineHeight: '1.5' }}>
          <strong style={{ color: '#93c5fd', display: 'block', marginBottom: '0.2rem' }}>
            Important Note:
          </strong>
          Extracted questions are not published until you review them.
          <span style={{ display: 'block', fontSize: '0.82rem', color: '#bfdbfe', marginTop: '0.2rem' }}>
            The test remains in <strong>DRAFT</strong> status. You can review options, set correct answers, and adjust marking schemes before publishing.
          </span>
        </div>
      </div>

      {/* Amber Banner: SCANNED_PDF_NOT_SUPPORTED */}
      {scannedPdfError && (
        <div
          id="scanned-pdf-error-banner"
          className="alert-box alert-amber"
          style={{
            display: 'flex',
            alignItems: 'flex-start',
            gap: '0.85rem',
            animation: 'fadeIn 0.25s ease',
          }}
        >
          <AlertTriangle size={22} color="#f59e0b" style={{ flexShrink: 0, marginTop: '2px' }} />
          <div style={{ flex: 1 }}>
            <strong style={{ color: '#fbbf24', fontSize: '0.95rem', display: 'block', marginBottom: '0.25rem' }}>
              Scanned PDF Detected
            </strong>
            <p style={{ margin: 0, lineHeight: '1.45' }}>{scannedPdfError}</p>
            <p style={{ margin: '0.5rem 0 0', fontSize: '0.82rem', color: '#fef3c7' }}>
              Tip: Export your document directly as PDF from Microsoft Word, Google Docs, or LaTeX to preserve selectable text.
            </p>
          </div>
        </div>
      )}

      {/* Red Banner: Other Errors */}
      {generalError && (
        <div
          id="general-error-banner"
          className="alert-box alert-error"
          style={{
            display: 'flex',
            alignItems: 'flex-start',
            gap: '0.85rem',
            animation: 'fadeIn 0.25s ease',
          }}
        >
          <AlertCircle size={22} color="#f43f5e" style={{ flexShrink: 0, marginTop: '2px' }} />
          <div style={{ flex: 1 }}>
            <strong style={{ color: '#fda4af', fontSize: '0.95rem', display: 'block', marginBottom: '0.25rem' }}>
              Upload / Parsing Failed
            </strong>
            <p style={{ margin: 0, lineHeight: '1.45' }}>{generalError}</p>
          </div>
        </div>
      )}

      {/* Success Summary View */}
      {step === 'success' && parseResult && (
        <div
          id="success-summary-card"
          className="form-card"
          style={{
            border: '1px solid rgba(16, 185, 129, 0.4)',
            boxShadow: '0 4px 24px rgba(16, 185, 129, 0.12)',
            marginBottom: '1.75rem',
            animation: 'fadeIn 0.3s ease',
          }}
        >
          {/* Header Summary */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem', marginBottom: '1.25rem' }}>
            <div
              style={{
                width: '42px',
                height: '42px',
                borderRadius: '50%',
                backgroundColor: 'rgba(16, 185, 129, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#34d399',
              }}
            >
              <CheckCircle2 size={24} />
            </div>
            <div>
              <h2 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-primary)', margin: 0 }}>
                {parseResult.questionsCreated} questions extracted, {parseResult.warnings.length}{' '}
                {parseResult.warnings.length === 1 ? 'warning' : 'warnings'}
              </h2>
              <p style={{ margin: '0.2rem 0 0', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                Questions saved with default marks (+4 / -1) and correct_option set to NULL. Test status remains{' '}
                <span className="status-pill status-draft" style={{ padding: '1px 6px', fontSize: '0.75rem' }}>
                  DRAFT
                </span>
              </p>
            </div>
          </div>

          {/* Warning List Section */}
          {parseResult.warnings.length > 0 ? (
            <div style={{ marginBottom: '1.5rem' }}>
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.45rem',
                  fontSize: '0.9rem',
                  fontWeight: 600,
                  color: '#fbbf24',
                  marginBottom: '0.75rem',
                }}
              >
                <AlertTriangle size={16} />
                <span>Warnings to Review ({parseResult.warnings.length})</span>
              </div>
              <div style={{ maxHeight: '240px', overflowY: 'auto', paddingRight: '0.35rem' }}>
                {parseResult.warnings.map((warning, idx) => (
                  <div key={idx} className="warning-item">
                    <span style={{ color: '#f59e0b', fontWeight: 700 }}>•</span>
                    <span>{warning}</span>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div
              style={{
                padding: '0.85rem 1rem',
                backgroundColor: 'rgba(16, 185, 129, 0.08)',
                border: '1px solid rgba(16, 185, 129, 0.2)',
                borderRadius: 'var(--radius-sm)',
                fontSize: '0.88rem',
                color: '#a7f3d0',
                marginBottom: '1.5rem',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
              }}
            >
              <CheckCircle2 size={16} color="#34d399" />
              <span>All questions parsed cleanly with 4 options and valid question texts.</span>
            </div>
          )}

          {/* Action Buttons */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem', flexWrap: 'wrap' }}>
            <Link
              id="review-questions-btn"
              to={`/tests/${testId}/questions`}
              className="btn-primary"
              style={{ textDecoration: 'none', padding: '0.85rem 1.75rem', fontSize: '0.95rem' }}
            >
              <Edit3 size={17} /> Review Questions
            </Link>

            <button
              type="button"
              className="btn-secondary"
              onClick={handleClearFile}
              title="Upload another PDF file"
            >
              <Upload size={15} /> Upload Another PDF
            </button>

            <Link to="/tests" className="btn-secondary" style={{ textDecoration: 'none' }}>
              Back to Tests
            </Link>
          </div>
        </div>
      )}

      {/* Main Upload Form / Dropzone Card (hidden or collapsed after success unless requested) */}
      {step !== 'success' && (
        <div className="form-card" style={{ marginBottom: '2rem' }}>
          <input
            ref={fileInputRef}
            id="pdf-file-input"
            type="file"
            accept=".pdf,application/pdf"
            onChange={handleInputChange}
            style={{ display: 'none' }}
          />

          {/* Drag & Drop Zone */}
          <div
            id="upload-dropzone"
            className={`upload-dropzone ${isDragOver ? 'dragging' : ''}`}
            onDragEnter={handleDragEnter}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
          >
            <div
              style={{
                width: '56px',
                height: '56px',
                borderRadius: '50%',
                backgroundColor: isDragOver ? 'var(--teal-primary)' : 'var(--teal-glow)',
                color: isDragOver ? '#fff' : 'var(--teal-hover)',
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: '1.25rem',
                transition: 'all 0.2s ease',
              }}
            >
              <Upload size={28} />
            </div>

            <h3 style={{ fontSize: '1.15rem', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.45rem' }}>
              Drag and drop your question paper PDF here
            </h3>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginBottom: '1.25rem' }}>
              or <span style={{ color: 'var(--teal-primary)', fontWeight: 600, textDecoration: 'underline' }}>browse from your computer</span>
            </p>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              <span>Format: <strong>PDF (.pdf)</strong></span>
              <span>•</span>
              <span>Max size: <strong>10 MB</strong></span>
              <span>•</span>
              <span>Type: <strong>Text-based</strong> (not scanned image)</span>
            </div>
          </div>

          {/* Selected File Details Card */}
          {selectedFile && (
            <div
              id="selected-file-card"
              className="file-preview-card"
              style={{ marginTop: '1.25rem' }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
                <div
                  style={{
                    width: '40px',
                    height: '40px',
                    borderRadius: 'var(--radius-sm)',
                    backgroundColor: 'var(--teal-glow)',
                    color: 'var(--teal-primary)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <FileText size={22} />
                </div>
                <div>
                  <div style={{ fontWeight: 600, color: 'var(--text-primary)', fontSize: '0.92rem' }}>
                    {selectedFile.name}
                  </div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.15rem' }}>
                    {formatFileSize(selectedFile.size)} • PDF Document
                  </div>
                </div>
              </div>

              {step === 'idle' && (
                <button
                  type="button"
                  onClick={handleClearFile}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: 'var(--text-muted)',
                    cursor: 'pointer',
                    padding: '0.35rem',
                    borderRadius: '4px',
                    display: 'flex',
                    alignItems: 'center',
                  }}
                  title="Remove selected file"
                >
                  <X size={18} />
                </button>
              )}
            </div>
          )}

          {/* Progress State UI */}
          {(step === 'uploading' || step === 'parsing') && (
            <div style={{ marginTop: '1.5rem', padding: '1.25rem', backgroundColor: 'rgba(0, 0, 0, 0.25)', borderRadius: 'var(--radius-md)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.88rem' }}>
                <span style={{ color: 'var(--teal-primary)', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                  <RefreshCw size={14} className="spin" /> {statusMessage}
                </span>
                <span style={{ color: 'var(--text-muted)', fontFamily: 'monospace' }}>
                  {progressPercent}%
                </span>
              </div>

              <div className="progress-track">
                <div className="progress-fill" style={{ width: `${progressPercent}%` }} />
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.45rem' }}>
                <span style={{ color: progressPercent >= 35 ? 'var(--teal-hover)' : 'inherit' }}>
                  1. Upload file
                </span>
                <span style={{ color: progressPercent >= 75 ? 'var(--teal-hover)' : 'inherit' }}>
                  2. Extract text & parse
                </span>
                <span style={{ color: progressPercent === 100 ? 'var(--teal-hover)' : 'inherit' }}>
                  3. Ready to review
                </span>
              </div>
            </div>
          )}

          {/* Upload & Parse Action Button */}
          <div style={{ marginTop: '1.75rem', display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '0.85rem' }}>
            <Link to="/tests" className="btn-secondary" style={{ textDecoration: 'none' }}>
              Cancel
            </Link>

            <button
              id="upload-parse-btn"
              type="button"
              className="btn-primary"
              disabled={!selectedFile || step === 'uploading' || step === 'parsing'}
              onClick={handleUploadAndParse}
              style={{ minWidth: '220px', justifyContent: 'center' }}
            >
              {step === 'uploading' || step === 'parsing' ? (
                <>
                  <RefreshCw size={16} className="spin" />
                  <span>Processing...</span>
                </>
              ) : (
                <>
                  <Sparkles size={16} />
                  <span>Upload & Parse Questions</span>
                </>
              )}
            </button>
          </div>
        </div>
      )}

      {/* Helpful Parsing Instructions Card */}
      <div
        style={{
          backgroundColor: 'rgba(255, 255, 255, 0.02)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-md)',
          padding: '1.25rem 1.5rem',
          fontSize: '0.85rem',
          color: 'var(--text-secondary)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.5rem' }}>
          <FileQuestion size={16} color="var(--teal-primary)" />
          <span>Expected Question Paper Format</span>
        </div>
        <ul style={{ paddingLeft: '1.2rem', lineHeight: '1.6' }}>
          <li>
            <strong>Questions</strong> should start with a number pattern like <code>1.</code>, <code>1)</code>, <code>Q1.</code>, or <code>Q.1</code>.
          </li>
          <li>
            <strong>Options</strong> should start with <code>A.</code>, <code>A)</code>, <code>(A)</code> through <code>D</code>.
          </li>
          <li>
            Continuation lines are automatically preserved and stitched to the active question or option.
          </li>
          <li>
            Header and footer noise like <code>Page 2 of 5</code> is automatically filtered out.
          </li>
        </ul>
      </div>
    </div>
  );
};
