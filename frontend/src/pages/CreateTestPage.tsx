import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { adminApi, ApiError } from '../api/adminApi';
import { TestRequest } from '../types';
import {
  PlusCircle,
  AlertCircle,
  CheckCircle2,
  ShieldAlert,
  ArrowLeft,
  ListOrdered
} from 'lucide-react';

export const CreateTestPage: React.FC = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState<TestRequest>({
    title: '',
    instructions: '',
    durationMinutes: 60,
    accessType: 'FREE',
    price: 0,
    maxAttempts: 1,
    maxSolutionViews: 3,
    maxPdfDownloads: 1,
  });

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [generalError, setGeneralError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value, type } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'number' ? (value === '' ? '' : Number(value)) : value,
    }));

    // Clear specific field error when user modifies field
    if (fieldErrors[name]) {
      setFieldErrors((prev) => {
        const next = { ...prev };
        delete next[name];
        return next;
      });
    }
  };

  const handleAccessTypeChange = (type: 'FREE' | 'PAID') => {
    setFormData((prev) => ({
      ...prev,
      accessType: type,
      price: type === 'FREE' ? 0 : (prev.price && prev.price > 0 ? prev.price : 99),
    }));

    if (fieldErrors.price) {
      setFieldErrors((prev) => {
        const next = { ...prev };
        delete next.price;
        return next;
      });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFieldErrors({});
    setGeneralError(null);
    setSuccessMessage(null);
    setIsSubmitting(true);

    try {
      const payload: TestRequest = {
        ...formData,
        durationMinutes: Number(formData.durationMinutes),
        maxAttempts: Number(formData.maxAttempts),
        maxSolutionViews: Number(formData.maxSolutionViews),
        maxPdfDownloads: Number(formData.maxPdfDownloads),
        price: formData.accessType === 'FREE' ? 0 : Number(formData.price),
      };

      const created = await adminApi.createTest(payload);
      setSuccessMessage(`Test "${created.title}" successfully created as DRAFT (ID: ${created.id})`);

      // Reset form
      setFormData({
        title: '',
        instructions: '',
        durationMinutes: 60,
        accessType: 'FREE',
        price: 0,
        maxAttempts: 1,
        maxSolutionViews: 3,
        maxPdfDownloads: 1,
      });
    } catch (err: any) {
      if (err instanceof ApiError && err.response?.errors && err.response.errors.length > 0) {
        const errorMap: Record<string, string> = {};
        err.response.errors.forEach((fe) => {
          errorMap[fe.field] = fe.message;
        });
        setFieldErrors(errorMap);
        setGeneralError('Please correct the validation errors below.');
      } else {
        setGeneralError(err?.response?.message || err?.message || 'Failed to create test series.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title">Create Test Series</h1>
          <p className="page-subtitle">Configure test metadata, timing limits, and access pricing</p>
        </div>
        <Link to="/tests" className="btn-secondary" style={{ textDecoration: 'none' }}>
          <ArrowLeft size={16} /> Back to Tests
        </Link>
      </div>

      {generalError && (
        <div className="alert-box alert-error">
          <AlertCircle size={20} style={{ flexShrink: 0 }} />
          <div>
            <strong>Error: </strong>
            <span>{generalError}</span>
          </div>
        </div>
      )}

      {successMessage && (
        <div className="alert-box alert-success">
          <CheckCircle2 size={20} style={{ flexShrink: 0 }} />
          <div style={{ flex: 1 }}>
            <strong>Success! </strong>
            <span>{successMessage}</span>
            <div style={{ marginTop: '0.65rem' }}>
              <Link to="/tests" className="btn-sm btn-primary" style={{ textDecoration: 'none' }}>
                <ListOrdered size={14} /> View in Tests Table
              </Link>
            </div>
          </div>
        </div>
      )}

      <div className="form-card">
        <form onSubmit={handleSubmit}>
          {/* Title Field */}
          <div className="form-group">
            <label className="form-label" htmlFor="title">
              Test Title <span className="required-star">*</span>
            </label>
            <input
              id="title"
              name="title"
              type="text"
              className={`form-input ${fieldErrors.title ? 'has-error' : ''}`}
              placeholder="e.g. JEE Advanced Full Physics Mock #01"
              value={formData.title}
              onChange={handleChange}
            />
            {fieldErrors.title && (
              <span className="field-error">
                <ShieldAlert size={14} /> {fieldErrors.title}
              </span>
            )}
          </div>

          {/* Instructions Field */}
          <div className="form-group">
            <label className="form-label" htmlFor="instructions">
              Instructions
            </label>
            <textarea
              id="instructions"
              name="instructions"
              rows={3}
              className={`form-textarea ${fieldErrors.instructions ? 'has-error' : ''}`}
              placeholder="Instructions displayed to students before commencing the assessment..."
              value={formData.instructions}
              onChange={handleChange}
            />
            {fieldErrors.instructions && (
              <span className="field-error">
                <ShieldAlert size={14} /> {fieldErrors.instructions}
              </span>
            )}
          </div>

          {/* Duration & Access Type Grid */}
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label" htmlFor="durationMinutes">
                <span>Duration (minutes) <span className="required-star">*</span></span>
              </label>
              <div style={{ position: 'relative' }}>
                <input
                  id="durationMinutes"
                  name="durationMinutes"
                  type="number"
                  min="1"
                  className={`form-input ${fieldErrors.durationMinutes ? 'has-error' : ''}`}
                  value={formData.durationMinutes}
                  onChange={handleChange}
                />
              </div>
              {fieldErrors.durationMinutes && (
                <span className="field-error">
                  <ShieldAlert size={14} /> {fieldErrors.durationMinutes}
                </span>
              )}
            </div>

            <div className="form-group">
              <label className="form-label">
                Access Type <span className="required-star">*</span>
              </label>
              <div className="segmented-control">
                <button
                  type="button"
                  className={`segmented-btn ${formData.accessType === 'FREE' ? 'active' : ''}`}
                  onClick={() => handleAccessTypeChange('FREE')}
                >
                  FREE
                </button>
                <button
                  type="button"
                  className={`segmented-btn ${formData.accessType === 'PAID' ? 'active' : ''}`}
                  onClick={() => handleAccessTypeChange('PAID')}
                >
                  PAID
                </button>
              </div>
            </div>
          </div>

          {/* Price Field - Only shown when PAID */}
          {formData.accessType === 'PAID' && (
            <div className="form-group" style={{ animation: 'fadeIn 0.2s ease' }}>
              <label className="form-label" htmlFor="price">
                Price (INR ₹) <span className="required-star">* (PAID requires price &gt; 0)</span>
              </label>
              <input
                id="price"
                name="price"
                type="number"
                step="0.01"
                min="0.01"
                className={`form-input ${fieldErrors.price ? 'has-error' : ''}`}
                placeholder="499.00"
                value={formData.price}
                onChange={handleChange}
              />
              {fieldErrors.price && (
                <span className="field-error">
                  <ShieldAlert size={14} /> {fieldErrors.price}
                </span>
              )}
            </div>
          )}

          {/* Attempts and Solution Limits Grid */}
          <div className="form-grid" style={{ marginTop: '0.5rem' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="maxAttempts">
                Max Attempts <span className="required-star">*</span>
              </label>
              <input
                id="maxAttempts"
                name="maxAttempts"
                type="number"
                min="1"
                className={`form-input ${fieldErrors.maxAttempts ? 'has-error' : ''}`}
                value={formData.maxAttempts}
                onChange={handleChange}
              />
              {fieldErrors.maxAttempts && (
                <span className="field-error">
                  <ShieldAlert size={14} /> {fieldErrors.maxAttempts}
                </span>
              )}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="maxSolutionViews">
                Max Solution Views <span className="required-star">*</span>
              </label>
              <input
                id="maxSolutionViews"
                name="maxSolutionViews"
                type="number"
                min="0"
                className={`form-input ${fieldErrors.maxSolutionViews ? 'has-error' : ''}`}
                value={formData.maxSolutionViews}
                onChange={handleChange}
              />
              {fieldErrors.maxSolutionViews && (
                <span className="field-error">
                  <ShieldAlert size={14} /> {fieldErrors.maxSolutionViews}
                </span>
              )}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="maxPdfDownloads">
                Max PDF Downloads <span className="required-star">*</span>
              </label>
              <input
                id="maxPdfDownloads"
                name="maxPdfDownloads"
                type="number"
                min="0"
                className={`form-input ${fieldErrors.maxPdfDownloads ? 'has-error' : ''}`}
                value={formData.maxPdfDownloads}
                onChange={handleChange}
              />
              {fieldErrors.maxPdfDownloads && (
                <span className="field-error">
                  <ShieldAlert size={14} /> {fieldErrors.maxPdfDownloads}
                </span>
              )}
            </div>
          </div>

          {/* Form Actions */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.75rem', paddingTop: '1.25rem', borderTop: '1px solid var(--border-subtle)' }}>
            <button
              type="button"
              className="btn-secondary"
              onClick={() => navigate('/tests')}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={isSubmitting}
            >
              <PlusCircle size={18} />
              {isSubmitting ? 'Creating...' : 'Create Test Series'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
