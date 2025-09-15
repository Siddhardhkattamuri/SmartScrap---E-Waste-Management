import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';

const ProtectedRoute = () => {
  const { isAuthenticated, loading } = useAuth();

  // Show a loading indicator while checking auth status
  if (loading) {
    return <div>Loading...</div>;
  }

  // If authenticated, render the child route. Otherwise, redirect to login.
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" />;
};

export default ProtectedRoute;