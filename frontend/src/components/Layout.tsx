import React, { useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { useRole } from '../context/RoleContext';
import {
  LayoutDashboard,
  FilePlus2,
  ListOrdered,
  Award,
  Menu,
  X,
  Sparkles,
} from 'lucide-react';

export const Layout: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { role, setRole } = useRole();
  const location = useLocation();

  // Close mobile sidebar on route transition
  React.useEffect(() => {
    setMobileMenuOpen(false);
  }, [location.pathname]);

  const navItems = [
    { label: 'Dashboard', path: '/', icon: LayoutDashboard },
    { label: 'Create Test', path: '/create-test', icon: FilePlus2 },
    { label: 'Tests', path: '/tests', icon: ListOrdered },
    { label: 'Results', path: '/results', icon: Award },
  ];

  return (
    <div className="app-layout">
      {/* Mobile background overlay */}
      <div
        className={`mobile-overlay ${mobileMenuOpen ? 'open' : ''}`}
        onClick={() => setMobileMenuOpen(false)}
        aria-hidden="true"
      />

      {/* Sidebar Navigation */}
      <aside className={`sidebar ${mobileMenuOpen ? 'open' : ''}`}>
        <div className="sidebar-header">
          <NavLink to="/" className="brand-badge">
            <div className="brand-logo-icon">C</div>
            <div>
              <div className="brand-title">Crevolab</div>
              <div className="brand-subtitle">Test Series</div>
            </div>
          </NavLink>
          <button
            className="hamburger-btn"
            onClick={() => setMobileMenuOpen(false)}
            aria-label="Close menu"
          >
            <X size={20} />
          </button>
        </div>

        <nav className="sidebar-nav">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              >
                <Icon className="nav-icon" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>
      </aside>

      {/* Main Container */}
      <div className="main-wrapper">
        <header className="topbar">
          <div className="topbar-left">
            <button
              className="hamburger-btn"
              onClick={() => setMobileMenuOpen(true)}
              aria-label="Open sidebar menu"
            >
              <Menu size={22} />
            </button>
          </div>

          <div className="topbar-right">
            {/* DEMO-ONLY: Role toggle between Admin and Student labelled "Demo mode" */}
            <div className="demo-role-container">
              <span className="demo-role-label">
                <Sparkles size={12} style={{ display: 'inline', marginRight: 4 }} />
                Demo mode
              </span>
              <div className="role-pill-group">
                <button
                  type="button"
                  className={`role-pill-btn ${role === 'Admin' ? 'active' : ''}`}
                  onClick={() => setRole('Admin')}
                >
                  Admin
                </button>
                <button
                  type="button"
                  className={`role-pill-btn ${role === 'Student' ? 'active' : ''}`}
                  onClick={() => setRole('Student')}
                >
                  Student
                </button>
              </div>
            </div>
          </div>
        </header>

        <main className="content-area">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
