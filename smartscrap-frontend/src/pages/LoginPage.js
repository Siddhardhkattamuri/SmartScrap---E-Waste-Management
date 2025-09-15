import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import api from '../api/apiService';
import styles from './FormPage.module.css';

const LoginPage = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    otp: '',
  });
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [showOtpInput, setShowOtpInput] = useState(false);
  const { loginWithOtp } = useAuth();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // Step 1: Verify credentials and send OTP
  const handleCredentialsSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);
    
    try {
      // Send credentials to backend for verification and OTP generation
      await api.post('/auth/login-otp/init', {
        email: formData.email,
        password: formData.password
      });
      
      setShowOtpInput(true);
      setMessage('Credentials verified! OTP sent to your email. Please check your inbox.');
      // Clear password field for security
      setFormData({ ...formData, password: '', otp: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Step 2: Verify OTP and complete login
  const handleOtpVerify = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);
    
    try {
      console.log('Attempting OTP verification with:', { email: formData.email, otp: formData.otp });
      await loginWithOtp(formData.email, formData.otp);
      // Clear OTP field on success
      setFormData({ ...formData, otp: '' });
      setShowOtpInput(false);
      // loginWithOtp() handles navigation
    } catch (err) {
      console.error('OTP verification error:', err);
      setError(err.response?.data?.message || 'Invalid OTP. Please try again.');
      // Clear OTP field on error too
      setFormData({ ...formData, otp: '' });
    } finally {
      setLoading(false);
    }
  };

  const handleResendOtp = async () => {
    setError('');
    setMessage('');
    setLoading(true);
    
    try {
      // Resend OTP with credentials
      await api.post('/auth/login-otp/init', {
        email: formData.email,
        password: formData.password
      });
      setMessage('New OTP sent to your email. Please check your inbox.');
      // Clear OTP field
      setFormData({ ...formData, otp: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to resend OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleBackToCredentials = () => {
    setShowOtpInput(false);
    setFormData({ ...formData, password: '', otp: '' });
    setError('');
    setMessage('');
  };

  return (
    <div className={styles.pageContainer}>
      <div className={styles.formWrapper}>
        <h2 className={styles.title}>Login to Your Account</h2>
        
        {!showOtpInput ? (
          // Step 1: Credentials Form
          <form onSubmit={handleCredentialsSubmit} className={styles.form}>
            <div className={styles.inputGroup}>
              <label htmlFor="email" className={styles.label}>Email Address</label>
              <input 
                id="email" 
                name="email" 
                type="email" 
                value={formData.email} 
                onChange={handleChange} 
                className={styles.input} 
                required 
              />
            </div>
            <div className={styles.inputGroup}>
              <label htmlFor="password" className={styles.label}>Password</label>
              <input 
                id="password" 
                name="password" 
                type="password" 
                value={formData.password} 
                onChange={handleChange} 
                className={styles.input} 
                required 
              />
            </div>
            {error && <p className={styles.errorMessage}>{error}</p>}
            {message && <p className={styles.successMessage}>{message}</p>}
            <button type="submit" className={styles.submitButton} disabled={loading}>
              {loading ? 'Verifying...' : 'Verify & Send OTP'}
            </button>
          </form>
        ) : (
          // Step 2: OTP Verification Form
          <form onSubmit={handleOtpVerify} className={styles.form}>
            <div className={styles.inputGroup}>
              <label htmlFor="otp" className={styles.label}>Enter OTP</label>
              <input 
                id="otp" 
                name="otp" 
                type="text" 
                value={formData.otp} 
                onChange={handleChange} 
                className={styles.input} 
                placeholder="Enter 6-digit OTP"
                maxLength="6"
                pattern="[0-9]{6}"
                inputMode="numeric"
                required 
              />
              <small>Check your email for the 6-digit code sent to {formData.email}</small>
            </div>
            {error && <p className={styles.errorMessage}>{error}</p>}
            {message && <p className={styles.successMessage}>{message}</p>}
            <div className={styles.buttonGroup}>
              <button type="submit" className={styles.submitButton} disabled={loading || formData.otp.length !== 6}>
                {loading ? 'Verifying...' : 'Verify OTP & Login'}
              </button>
              <button 
                type="button" 
                className={styles.secondaryButton}
                onClick={handleResendOtp}
                disabled={loading}
              >
                {loading ? 'Sending...' : 'Resend OTP'}
              </button>
              <button 
                type="button" 
                className={styles.secondaryButton}
                onClick={handleBackToCredentials}
                disabled={loading}
              >
                Back to Credentials
              </button>
            </div>
          </form>
        )}

        <p className={styles.redirectText}>
          Don't have an account? <Link to="/register" className={styles.redirectLink}>Register here</Link>
        </p>
      </div>
    </div>
  );
};

export default LoginPage;