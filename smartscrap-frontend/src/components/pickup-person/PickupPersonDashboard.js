import React, { useState, useEffect } from 'react';
import api from '../../api/apiService';
import styles from './PickupPersonDashboard.module.css';
import { FiMapPin, FiClock, FiUser, FiPhone, FiMail, FiPackage, FiCheckCircle, FiXCircle, FiNavigation, FiShield } from 'react-icons/fi';

const PickupPersonDashboard = () => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showOtpModal, setShowOtpModal] = useState(false);
  const [selectedRequestId, setSelectedRequestId] = useState(null);
  const [otp, setOtp] = useState('');
  const [otpLoading, setOtpLoading] = useState(false);
  const [otpMessage, setOtpMessage] = useState('');
  const [loadingRequests, setLoadingRequests] = useState(new Set());

  useEffect(() => {
    fetchPickupRequests();
  }, []);

  const fetchPickupRequests = async () => {
    try {
      setLoading(true);
      const response = await api.get('/pickup-person/dashboard');
      setRequests(response.data);
    } catch (err) {
      setError('Failed to fetch pickup requests');
      console.error('Error fetching pickup requests:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Not scheduled';
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING': return '#ffc107';
      case 'APPROVED': return '#17a2b8';
      case 'SCHEDULED': return '#007bff';
      case 'COMPLETED': return '#28a745';
      case 'REJECTED': return '#dc3545';
      case 'CANCELLED': return '#6c757d';
      default: return '#6c757d';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'SCHEDULED': return <FiClock />;
      case 'COMPLETED': return <FiCheckCircle />;
      case 'CANCELLED': return <FiXCircle />;
      default: return <FiPackage />;
    }
  };

  const handleInitiateCompletion = async (requestId) => {
    console.log('[FRONTEND] Initiating completion for request:', requestId);
    try {
      // Add this request to loading set
      setLoadingRequests(prev => new Set(prev).add(requestId));
      setOtpMessage('');
      console.log('[FRONTEND] Making API call to initiate completion...');
      const response = await api.post(`/pickup-person/requests/${requestId}/initiate-completion`);
      console.log('[FRONTEND] API response:', response.data);
      setOtpMessage(response.data);
      setSelectedRequestId(requestId);
      setShowOtpModal(true);
    } catch (err) {
      console.error('[FRONTEND] Failed to initiate completion:', err);
      console.error('[FRONTEND] Error details:', err.response?.data);
      console.error('[FRONTEND] Error status:', err.response?.status);
      console.error('[FRONTEND] Error message:', err.message);
      
      let errorMessage = 'Failed to send OTP. Please try again.';
      
      // Try to extract the actual error message
      if (err.response?.data) {
        if (typeof err.response.data === 'string') {
          errorMessage += '\n\nError: ' + err.response.data;
        } else if (err.response.data.message) {
          errorMessage += '\n\nError: ' + err.response.data.message;
        } else {
          errorMessage += '\n\nError: ' + JSON.stringify(err.response.data);
        }
      } else if (err.message) {
        errorMessage += '\n\nError: ' + err.message;
      }
      
      console.error('[FRONTEND] Final error message:', errorMessage);
      alert(errorMessage);
    } finally {
      // Remove this request from loading set
      setLoadingRequests(prev => {
        const newSet = new Set(prev);
        newSet.delete(requestId);
        return newSet;
      });
    }
  };

  const handleOtpVerification = async () => {
    if (!otp || otp.length !== 6) {
      alert('Please enter a valid 6-digit OTP');
      return;
    }

    try {
      setOtpLoading(true);
      await api.post(`/pickup-person/requests/${selectedRequestId}/verify-otp`, { otp });
      setShowOtpModal(false);
      setOtp('');
      setOtpMessage('');
      // Clear loading state for the completed request
      if (selectedRequestId) {
        setLoadingRequests(prev => {
          const newSet = new Set(prev);
          newSet.delete(selectedRequestId);
          return newSet;
        });
      }
      fetchPickupRequests(); // Refresh the list
      alert('Request completed successfully! Completion emails have been sent.');
    } catch (err) {
      console.error('Failed to verify OTP:', err);
      alert('Invalid or expired OTP. Please try again.');
    } finally {
      setOtpLoading(false);
    }
  };

  const closeOtpModal = () => {
    setShowOtpModal(false);
    setOtp('');
    setOtpMessage('');
    setSelectedRequestId(null);
    // Clear loading state for the selected request
    if (selectedRequestId) {
      setLoadingRequests(prev => {
        const newSet = new Set(prev);
        newSet.delete(selectedRequestId);
        return newSet;
      });
    }
  };

  const openGoogleMaps = (address) => {
    const encodedAddress = encodeURIComponent(address);
    window.open(`https://www.google.com/maps/search/?api=1&query=${encodedAddress}`, '_blank');
  };

  const testApiConnection = async () => {
    try {
      console.log('[FRONTEND] Testing API connection...');
      const response = await api.get('/pickup-person/test-connection');
      console.log('[FRONTEND] API connection test response:', response.data);
      alert('API connection test successful: ' + response.data);
    } catch (err) {
      console.error('[FRONTEND] API connection test failed:', err);
      alert('API connection test failed: ' + (err.response?.data || err.message));
    }
  };

  const testHealthCheck = async () => {
    try {
      console.log('[FRONTEND] Running health check...');
      const response = await api.get('/pickup-person/health-check');
      console.log('[FRONTEND] Health check response:', response.data);
      alert('Health check results:\n' + response.data);
    } catch (err) {
      console.error('[FRONTEND] Health check failed:', err);
      alert('Health check failed: ' + (err.response?.data || err.message));
    }
  };


  if (loading) {
    return (
      <div className={styles.dashboardContainer}>
        <div className={styles.loadingContainer}>
          <div className={styles.loadingSpinner}></div>
          <p>Loading your pickup requests...</p>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.dashboardContainer}>
      {/* Header Section */}
      <div className={styles.header}>
        <div className={styles.headerContent}>
          <h1 className={styles.title}>🚛 Pickup Dashboard</h1>
          <p className={styles.subtitle}>Manage your assigned pickup requests</p>
          <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
            <button 
              onClick={testApiConnection}
              style={{
                background: '#007bff',
                color: 'white',
                border: 'none',
                padding: '8px 16px',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              Test API Connection
            </button>
            <button 
              onClick={testHealthCheck}
              style={{
                background: '#28a745',
                color: 'white',
                border: 'none',
                padding: '8px 16px',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              Health Check
            </button>
          </div>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className={styles.errorMessage}>
          <FiXCircle className={styles.errorIcon} />
          {error}
        </div>
      )}

      {/* Stats Section */}
      <div className={styles.statsContainer}>
        <div className={styles.statCard}>
          <div className={styles.statIcon}>
            <FiPackage />
          </div>
          <div className={styles.statContent}>
            <h3>Total Assigned</h3>
            <p className={styles.statNumber}>{requests.length}</p>
          </div>
        </div>
        <div className={styles.statCard}>
          <div className={styles.statIcon}>
            <FiClock />
          </div>
          <div className={styles.statContent}>
            <h3>Scheduled</h3>
            <p className={styles.statNumber}>
              {requests.filter(r => r.status === 'SCHEDULED').length}
            </p>
          </div>
        </div>
        <div className={styles.statCard}>
          <div className={styles.statIcon}>
            <FiCheckCircle />
          </div>
          <div className={styles.statContent}>
            <h3>Completed</h3>
            <p className={styles.statNumber}>
              {requests.filter(r => r.status === 'COMPLETED').length}
            </p>
          </div>
        </div>
      </div>

      {/* Requests Section */}
      <div className={styles.requestsSection}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>Your Pickup Requests</h2>
          <div className={styles.requestCount}>
            {requests.length} request{requests.length !== 1 ? 's' : ''}
          </div>
        </div>

        {requests.length === 0 ? (
          <div className={styles.noRequests}>
            <div className={styles.noRequestsIcon}>
              <FiPackage />
            </div>
            <h3>No Pickup Requests</h3>
            <p>You don't have any pickup requests assigned yet.</p>
          </div>
        ) : (
          <div className={styles.requestsGrid}>
            {requests.map((request) => (
              <div key={request.id} className={styles.requestCard}>
                {/* Card Header */}
                <div className={styles.cardHeader}>
                  <div className={styles.requestInfo}>
                    <span className={styles.requestId}>#{request.id}</span>
                    <span className={styles.requestDate}>
                      {new Date(request.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                  <div className={styles.statusContainer}>
                    <span 
                      className={styles.statusBadge}
                      style={{ backgroundColor: getStatusColor(request.status) }}
                    >
                      {getStatusIcon(request.status)}
                      {request.status}
                    </span>
                  </div>
                </div>

                {/* Card Body */}
                <div className={styles.cardBody}>
                  {/* Item Information */}
                  <div className={styles.itemSection}>
                    <h3 className={styles.itemTitle}>
                      {request.deviceType} - {request.brand} {request.model}
                    </h3>
                    <div className={styles.itemDetails}>
                      <div className={styles.detailItem}>
                        <span className={styles.detailLabel}>Condition:</span>
                        <span className={styles.detailValue}>{request.itemCondition}</span>
                      </div>
                      <div className={styles.detailItem}>
                        <span className={styles.detailLabel}>Quantity:</span>
                        <span className={styles.detailValue}>{request.quantity} item{request.quantity !== 1 ? 's' : ''}</span>
                      </div>
                    </div>
                  </div>

                  {/* Customer Information */}
                  <div className={styles.customerSection}>
                    <h4 className={styles.sectionTitle}>
                      <FiUser className={styles.sectionIcon} />
                      Customer Details
                    </h4>
                    <div className={styles.customerDetails}>
                      <div className={styles.customerItem}>
                        <FiUser className={styles.icon} />
                        <span>{request.customerName}</span>
                      </div>
                      <div className={styles.customerItem}>
                        <FiMail className={styles.icon} />
                        <span>{request.customerEmail}</span>
                      </div>
                      <div className={styles.customerItem}>
                        <FiPhone className={styles.icon} />
                        <span>{request.customerMobile}</span>
                      </div>
                    </div>
                  </div>

                  {/* Pickup Information */}
                  <div className={styles.pickupSection}>
                    <h4 className={styles.sectionTitle}>
                      <FiMapPin className={styles.sectionIcon} />
                      Pickup Information
                    </h4>
                    <div className={styles.pickupDetails}>
                      <div className={styles.pickupItem}>
                        <FiMapPin className={styles.icon} />
                        <span className={styles.address}>{request.pickupAddress}</span>
                        <button 
                          className={styles.mapButton}
                          onClick={() => openGoogleMaps(request.pickupAddress)}
                          title="Open in Google Maps"
                        >
                          <FiNavigation />
                        </button>
                      </div>
                      <div className={styles.pickupItem}>
                        <FiClock className={styles.icon} />
                        <span>{formatDate(request.pickupDate)}</span>
                      </div>
                    </div>
                  </div>

                  {/* Special Instructions */}
                  {request.remarks && (
                    <div className={styles.remarksSection}>
                      <h4 className={styles.sectionTitle}>
                        Special Instructions
                      </h4>
                      <p className={styles.remarksText}>{request.remarks}</p>
                    </div>
                  )}

                  {/* Images */}
                  {request.imagePaths && (
                    <div className={styles.imagesSection}>
                      <h4 className={styles.sectionTitle}>
                        Item Images
                      </h4>
                      <div className={styles.imageGrid}>
                        {request.imagePaths.split(',').map((path, index) => (
                          <img 
                            key={index}
                            src={`http://localhost:8080/uploads/${path.trim()}`}
                            alt={`Device ${index + 1}`}
                            className={styles.requestImage}
                            onError={(e) => {
                              e.target.style.display = 'none';
                            }}
                          />
                        ))}
                      </div>
                    </div>
                  )}
                </div>

                {/* Card Actions */}
                <div className={styles.cardActions}>
                  <div style={{fontSize: '12px', color: '#666', marginBottom: '5px'}}>
                    Status: {request.status}
                  </div>
                  {request.status === 'SCHEDULED' && (
                    <button 
                      className={styles.completeButton}
                      onClick={() => handleInitiateCompletion(request.id)}
                      disabled={loadingRequests.has(request.id)}
                    >
                      <FiShield />
                      {loadingRequests.has(request.id) ? 'Sending OTP...' : 'Complete Pickup'}
                    </button>
                  )}
                  {request.status !== 'SCHEDULED' && (
                    <div style={{fontSize: '12px', color: '#999'}}>
                      Only SCHEDULED requests can be completed
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* OTP Verification Modal */}
      {showOtpModal && (
        <div className={styles.otpModalBackdrop}>
          <div className={styles.otpModalContent}>
            <div className={styles.otpModalHeader}>
              <h3>
                <FiShield />
                OTP Verification Required
              </h3>
              <button 
                className={styles.closeOtpModal}
                onClick={closeOtpModal}
              >
                ×
              </button>
            </div>
            
            <div className={styles.otpModalBody}>
              {otpMessage && (
                <div className={styles.otpMessage}>
                  {otpMessage}
                </div>
              )}
              
              <p>Please ask the customer for the 6-digit OTP they received via email and enter it below:</p>
              
              <div className={styles.otpInputContainer}>
                <input
                  type="text"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  placeholder="Enter 6-digit OTP"
                  className={styles.otpInput}
                  maxLength="6"
                  autoFocus
                />
              </div>
              
              <div className={styles.otpModalActions}>
                <button 
                  className={styles.cancelOtpButton}
                  onClick={closeOtpModal}
                  disabled={otpLoading}
                >
                  Cancel
                </button>
                <button 
                  className={styles.verifyOtpButton}
                  onClick={handleOtpVerification}
                  disabled={otpLoading || otp.length !== 6}
                >
                  {otpLoading ? 'Verifying...' : 'Verify & Complete'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PickupPersonDashboard;