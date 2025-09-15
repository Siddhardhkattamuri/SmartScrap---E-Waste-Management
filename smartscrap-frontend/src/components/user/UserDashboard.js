import React, { useState, useEffect, useRef } from 'react';
import api from '../../api/apiService';
import styles from './UserDashboard.module.css';
import { FiClock, FiPlusCircle, FiUser } from 'react-icons/fi';

// Define the base URL for displaying images from the backend
const API_URL = 'http://localhost:8080';

const UserDashboard = () => {
    const fileInputRef = useRef(null);
    const [activeTab, setActiveTab] = useState('requests');
    const [myRequests, setMyRequests] = useState([]);
    const [profile, setProfile] = useState(null); // Start with null
    const [loading, setLoading] = useState(true);

    const [newItem, setNewItem] = useState({
        deviceType: 'Laptop', brand: '', model: '', condition: 'Working',
        quantity: 1, pickupAddress: '', remarks: ''
    });
    const [customDeviceType, setCustomDeviceType] = useState('');
    const [files, setFiles] = useState(null);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const clearMessages = () => {
        setError('');
        setSuccess('');
    };

    const handleTabChange = (tabName) => {
        clearMessages();
        setActiveTab(tabName);
    };

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            setError(''); // Clear previous errors on new fetch
            try {
                const profileResponse = await api.get('/user/profile');
                setProfile(profileResponse.data);

                // Use the fetched profile address to pre-fill the form
                setNewItem(prev => ({ ...prev, pickupAddress: profileResponse.data.address }));

                const requestsResponse = await api.get('/user/requests');
                setMyRequests(requestsResponse.data);

            } catch (err) {
                console.error("Failed to fetch user data:", err.response?.data || err.message);
                setError(err.response?.data?.message || "Could not load your dashboard data. Please try logging in again.");
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    const handleProfileChange = (e) => {
        setProfile({ ...profile, [e.target.name]: e.target.value });
    };

    const handleProfileUpdate = async (e) => {
        e.preventDefault();
        clearMessages();
        try {
            await api.put('/user/profile', profile);
            setSuccess("Profile updated successfully!");
        } catch (err) {
            setError(err.response?.data?.message || "Failed to update profile.");
        }
    };

    const handleNewItemChange = (e) => {
        setNewItem({ ...newItem, [e.target.name]: e.target.value });
        if (e.target.name === 'deviceType' && e.target.value !== 'Other') {
            setCustomDeviceType('');
        }
    };
    const handleFileChange = (e) => {
        setFiles(e.target.files);
    };

    const handleScheduleRequest = async (e) => {
        e.preventDefault();
        clearMessages();
        const formData = new FormData();
        const effectiveDeviceType = newItem.deviceType === 'Other' && customDeviceType.trim() !== ''
            ? customDeviceType.trim()
            : newItem.deviceType;
        formData.append('deviceType', effectiveDeviceType);
        Object.keys(newItem).forEach(key => {
            if (key !== 'deviceType') formData.append(key, newItem[key]);
        });
        if (files) {
            for (let i = 0; i < files.length; i++) {
                formData.append('files', files[i]);
            }
        }
        try {
            const response = await api.post('/user/requests', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });

            setSuccess('Request scheduled successfully!');
            setMyRequests([response.data, ...myRequests]);

            setNewItem({
                deviceType: 'Laptop', brand: '', model: '', condition: 'Working',
                quantity: 1, pickupAddress: profile.address, remarks: ''
            });
            setCustomDeviceType('');
            setFiles(null);
            if (fileInputRef.current) fileInputRef.current.value = "";

            setActiveTab('requests');
        } catch (err) {
            console.error("Schedule request error:", err.response?.data || err.message);
            setError(err.response?.data?.message || "An unknown error occurred. Please try again.");
        }
    };

    if (loading) return <div className={styles.loading}>Loading Dashboard...</div>;

    // --- THIS IS THE COMPLETE, CORRECT JSX TO RENDER THE DASHBOARD ---
    return (
        <div className={styles.dashboardContainer}>
            <h1 className={styles.header}>
                {profile ? `Welcome, ${profile.fullName}!` : "Welcome!"}
            </h1>

            {/* The error message will now display here if initial loading fails */}
            {error && !myRequests.length && <p className={styles.errorMessage}>{error}</p>}

            {/* The Tabs Section */}
            <div className={styles.tabs}>
                <button onClick={() => handleTabChange('requests')} className={activeTab === 'requests' ? styles.active : ''}>
                    <FiClock /> My Requests
                </button>
                <button onClick={() => handleTabChange('schedule')} className={activeTab === 'schedule' ? styles.active : ''}>
                    <FiPlusCircle /> Schedule Pickup
                </button>
                <button onClick={() => handleTabChange('profile')} className={activeTab === 'profile' ? styles.active : ''}>
                    <FiUser /> My Profile
                </button>
            </div>

            {/* The Tab Content Section */}
            <div className={styles.tabContent}>
                {activeTab === 'requests' && (
                    <div className={styles.requestsList}>
                        <h2>Your Pickup Requests</h2>
                        {myRequests.length === 0 ? <p>You have no scheduled requests.</p> : (
                            myRequests.map(req => (
                                // --- REPLACE THIS ENTIRE DIV ---
                                <div key={req.id} className={styles.requestCard}>
                                    <div className={styles.requestDetails}>
                                        <h3>{req.brand} {req.model} ({req.deviceType})</h3>
                                        <p><strong>Condition:</strong> {req.itemCondition} | <strong>Quantity:</strong> {req.quantity}</p>
                                        <p><strong>Requested on:</strong> {new Date(req.createdAt).toLocaleDateString()}</p>

                                        {/* --- NEW: Detailed Status Tracking Info --- */}
                                        <div className={styles.statusInfo}>
                                            {req.status === 'PENDING' && <p>Your request is waiting for admin approval.</p>}
                                            {req.status === 'APPROVED' && <p>Your request has been approved and will be scheduled for pickup soon.</p>}
                                            {req.status === 'REJECTED' && <p className={styles.rejection}><strong>Reason for Rejection:</strong> {req.rejectionReason || 'No reason provided.'}</p>}
                                            {req.status === 'SCHEDULED' && <p className={styles.scheduled}><strong>Pickup Scheduled for:</strong> {new Date(req.pickupDate).toLocaleString()}</p>}
                                            {req.status === 'COMPLETED' && <p className={styles.completed}>This pickup was completed. Thank you!</p>}
                                        </div>

                                        {req.remarks && <p><strong>Your Remarks:</strong> {req.remarks}</p>}
                                        <div className={styles.imageThumbnails}>
                                            {req.imagePaths && req.imagePaths.split(',').filter(p => p).map(path => (
                                                <a href={`${API_URL}/uploads/${path}`} target="_blank" rel="noopener noreferrer" key={path}>
                                                    <img src={`${API_URL}/uploads/${path}`} alt="e-waste item" />
                                                </a>
                                            ))}
                                        </div>
                                    </div>
                                    <span className={`${styles.statusBadge} ${styles[req.status.toLowerCase()]}`}>
                                        {req.status}
                                    </span>
                                </div>
                                // --- END OF REPLACEMENT ---
                            ))
                        )}
                    </div>
                )}

                {activeTab === 'schedule' && (
                    <form onSubmit={handleScheduleRequest} className={styles.form}>
                        <h2>Schedule a New Pickup</h2>
                        {error && <p className={styles.errorMessage}>{error}</p>}
                        {success && <p className={styles.successMessage}>{success}</p>}
                        <div className={styles.formGrid}>
                            <div className={styles.inputGroup}>
                                <label>Device Type</label>
                                <select name="deviceType" value={newItem.deviceType} onChange={handleNewItemChange}>
                                    <option>Laptop</option> <option>Mobile</option> <option>TV</option> <option>Printer</option> <option>Appliance</option> <option>Other</option>
                                </select>
                            </div>
                            {newItem.deviceType === 'Other' && (
                                <div className={styles.inputGroup}>
                                    <label>Specify Device Type</label>
                                    <input
                                        type="text"
                                        name="customDeviceType"
                                        value={customDeviceType}
                                        onChange={(e) => setCustomDeviceType(e.target.value)}
                                        placeholder="Enter device type"
                                        required
                                    />
                                    <small>Example: Router, Smartwatch, Tablet, etc.</small>
                                </div>
                            )}
                            <div className={styles.inputGroup}>
                                <label>Condition</label>
                                <select name="condition" value={newItem.condition} onChange={handleNewItemChange}>
                                    <option>Working</option> <option>Damaged</option> <option>Dead</option>
                                </select>
                            </div>
                            <div className={styles.inputGroup}>
                                <label>Brand</label>
                                <input type="text" name="brand" value={newItem.brand} onChange={handleNewItemChange} required />
                            </div>
                            <div className={styles.inputGroup}>
                                <label>Model</label>
                                <input type="text" name="model" value={newItem.model} onChange={handleNewItemChange} required />
                            </div>
                            <div className={`${styles.inputGroup} ${styles.fullWidth}`}>
                                <label>Quantity</label>
                                <input type="number" name="quantity" value={newItem.quantity} onChange={handleNewItemChange} min="1" required />
                            </div>
                            <div className={`${styles.inputGroup} ${styles.fullWidth}`}>
                                <label>Upload Images (Optional)</label>
                                <input type="file" name="files" onChange={handleFileChange} ref={fileInputRef} multiple />
                            </div>
                            <div className={`${styles.inputGroup} ${styles.fullWidth}`}>
                                <label>Pickup Address</label>
                                <textarea name="pickupAddress" placeholder="Leave blank to use your profile address" value={newItem.pickupAddress} onChange={handleNewItemChange} />
                            </div>
                            <div className={`${styles.inputGroup} ${styles.fullWidth}`}>
                                <label>Additional Remarks (Optional)</label>
                                <textarea name="remarks" value={newItem.remarks} onChange={handleNewItemChange} />
                            </div>
                        </div>
                        <button type="submit" className={styles.submitButton}>Schedule Now</button>
                    </form>
                )}

                {activeTab === 'profile' && (
                    <form onSubmit={handleProfileUpdate} className={styles.form}>
                        <h2>Update Your Profile</h2>
                        {error && <p className={styles.errorMessage}>{error}</p>}
                        {success && <p className={styles.successMessage}>{success}</p>}
                        <div className={styles.inputGroup}>
                            <label>Full Name</label>
                            <input type="text" name="fullName" value={profile.fullName} onChange={handleProfileChange} />
                        </div>
                        <div className={styles.inputGroup}>
                            <label>Email (cannot be changed)</label>
                            <input type="email" name="email" value={profile.email} readOnly disabled />
                        </div>
                        <div className={styles.inputGroup}>
                            <label>Mobile Number</label>
                            <input type="tel" name="mobileNumber" value={profile.mobileNumber} onChange={handleProfileChange} />
                        </div>
                        <div className={styles.inputGroup}>
                            <label>Address</label>
                            <textarea name="address" value={profile.address} onChange={handleProfileChange} />
                        </div>
                        <button type="submit" className={styles.submitButton}>Update Profile</button>
                    </form>
                )}
            </div>
        </div>
    );
};

export default UserDashboard;