import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { RoleProvider } from './context/RoleContext';
import { Layout } from './components/Layout';
import { DashboardPage } from './pages/DashboardPage';
import { CreateTestPage } from './pages/CreateTestPage';
import { TestsPage } from './pages/TestsPage';
import { ResultsPage } from './pages/ResultsPage';

export const App: React.FC = () => {
  return (
    <RoleProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<DashboardPage />} />
            <Route path="create-test" element={<CreateTestPage />} />
            <Route path="tests" element={<TestsPage />} />
            <Route path="results" element={<ResultsPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </RoleProvider>
  );
};

export default App;
