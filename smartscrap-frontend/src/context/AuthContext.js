import React, { createContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/apiService';
import { jwtDecode } from 'jwt-decode';

// Create the context
export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  // On component mount, check for existing user in local storage
  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      const parsedUser = JSON.parse(storedUser);
      try {
        const decoded = jwtDecode(parsedUser.token);
        // Check if token is expired
        if (decoded.exp * 1000 > Date.now()) {
          setUser(parsedUser);
        } else {
          // Token is expired, remove it
          localStorage.removeItem('user');
        }
      } catch (error) {
        // Invalid token, remove it
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  // Login function
  const login = async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    const userData = {
      token: response.data.token,
      id: response.data.id,
      email: response.data.email,
      roles: response.data.roles,
    };
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    navigate('/dashboard');
  };

  // OTP Login function
  const loginWithOtp = async (email, otp) => {
    const response = await api.post('/auth/login-otp/verify', { email, otp });
    const userData = {
      token: response.data.token,
      id: response.data.id,
      email: response.data.email,
      roles: response.data.roles,
    };
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    navigate('/dashboard');
  };

  // Logout function
  const logout = () => {
    localStorage.removeItem('user');
    setUser(null);
    navigate('/login');
  };

  const value = {
    user,
    login,
    loginWithOtp,
    logout,
    loading,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};