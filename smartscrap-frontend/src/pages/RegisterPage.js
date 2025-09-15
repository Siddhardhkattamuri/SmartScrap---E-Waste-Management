import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/apiService';
import styles from './FormPage.module.css';

const RegisterPage = () => {
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    mobileNumber: '',
    address: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await api.post('/auth/register', formData);
      navigate('/login', { state: { message: "Registration successful! Please log in." } });
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.pageContainer}>
      <div className={styles.formWrapper}>
        <h2 className={styles.title}>Create Your Account</h2>
        <form onSubmit={handleRegister} className={styles.form}>
          <div className={styles.inputGroup}>
            <label htmlFor="fullName" className={styles.label}>Full Name</label>
            <input id="fullName" name="fullName" type="text" onChange={handleChange} className={styles.input} required />
          </div>
          <div className={styles.inputGroup}>
            <label htmlFor="email" className={styles.label}>Email Address</label>
            <input id="email" name="email" type="email" onChange={handleChange} className={styles.input} required />
          </div>
          <div className={styles.inputGroup}>
            <label htmlFor="mobileNumber" className={styles.label}>Mobile Number</label>
            <input id="mobileNumber" name="mobileNumber" type="tel" onChange={handleChange} className={styles.input} required />
          </div>
          <div className={styles.inputGroup}>
            <label htmlFor="address" className={styles.label}>Full Address</label>
            <textarea id="address" name="address" onChange={handleChange} className={styles.textarea} required />
          </div>
          <div className={styles.inputGroup}>
            <label htmlFor="password" className={styles.label}>Password</label>
            <input id="password" name="password" type="password" onChange={handleChange} className={styles.input} required />
          </div>
          {error && <p className={styles.errorMessage}>{error}</p>}
          <button type="submit" className={styles.submitButton} disabled={loading}>
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>
        <p className={styles.redirectText}>
          Already have an account? <Link to="/login" className={styles.redirectLink}>Login here</Link>
        </p>
      </div>
    </div>
  );
};

export default RegisterPage;