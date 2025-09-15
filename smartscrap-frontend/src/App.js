import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/layout/Navbar';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import ProtectedRoute from './components/protect/ProtectedRoute';
import { AuthProvider } from './context/AuthContext';

function App() {
  return (
    <Router>
      <AuthProvider>
        <Navbar />
        <main>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Protected Routes */}
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<DashboardPage />} />
              {/* Add other protected routes here in the future */}
            </Route>

            {/* Optional: Add a 404 Not Found page */}
            <Route path="*" element={<h1>404 Not Found</h1>} />
          </Routes>
        </main>
      </AuthProvider>
    </Router>
  );
}

export default App;