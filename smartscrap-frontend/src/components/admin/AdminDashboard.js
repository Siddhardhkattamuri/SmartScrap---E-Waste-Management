import React, { useState, useEffect } from 'react';
import api from '../../api/apiService';
import styles from './AdminDashboard.module.css';
import { FiUsers, FiClipboard, FiCheckCircle, FiLoader, FiGrid, FiList } from 'react-icons/fi';

// --- NEW: A Modal component for pickup person registration ---
const PickupPersonModal = ({ onClose, onRegister }) => {
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: '',
        mobileNumber: '',
        address: ''
    });

    const handleSubmit = (e) => {
        e.preventDefault();
        onRegister(formData);
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    return (
        <div className={styles.modalBackdrop}>
            <div className={styles.modalContent}>
                <h3>Register New Pickup Person</h3>
                <form onSubmit={handleSubmit}>
                    <div className={styles.inputGroup}>
                        <label>Full Name</label>
                        <input 
                            type="text" 
                            name="fullName" 
                            value={formData.fullName} 
                            onChange={handleChange} 
                            required 
                        />
                    </div>
                    <div className={styles.inputGroup}>
                        <label>Email</label>
                        <input 
                            type="email" 
                            name="email" 
                            value={formData.email} 
                            onChange={handleChange} 
                            required 
                        />
                    </div>
                    <div className={styles.inputGroup}>
                        <label>Password</label>
                        <input 
                            type="password" 
                            name="password" 
                            value={formData.password} 
                            onChange={handleChange} 
                            required 
                        />
                    </div>
                    <div className={styles.inputGroup}>
                        <label>Mobile Number</label>
                        <input 
                            type="tel" 
                            name="mobileNumber" 
                            value={formData.mobileNumber} 
                            onChange={handleChange} 
                            required 
                        />
                    </div>
                    <div className={styles.inputGroup}>
                        <label>Address</label>
                        <textarea 
                            name="address" 
                            value={formData.address} 
                            onChange={handleChange} 
                            required 
                        />
                    </div>
                    <div className={styles.modalActions}>
                        <button type="submit" className={styles.updateButton}>Register</button>
                        <button type="button" onClick={onClose}>Cancel</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

// --- NEW: A Modal component for updates ---
const UpdateModal = ({ request, onClose, onUpdate, onAssignPickupPerson, pickupPersons }) => {
    const [status, setStatus] = useState(request.status);
    const [pickupDate, setPickupDate] = useState(request.pickupDate || '');
    const [rejectionReason, setRejectionReason] = useState(request.rejectionReason || '');
    const [selectedPickupPerson, setSelectedPickupPerson] = useState(request.pickupPersonId || '');

    const handleSubmit = () => {
        onUpdate(request.id, status, pickupDate, rejectionReason);
    };

    const handleAssignPickupPerson = () => {
        if (selectedPickupPerson && pickupDate) {
            onAssignPickupPerson(request.id, selectedPickupPerson, pickupDate);
        } else {
            alert('Please select a pickup person and date/time.');
        }
    };

    return (
        <div className={styles.modalBackdrop}>
            <div className={styles.modalContent}>
                <h3>Update Request #{request.id}</h3>
                <p><strong>Item:</strong> {request.brand} {request.model}</p>
                <div className={styles.inputGroup}>
                    <label>Status</label>
                    <select value={status} onChange={(e) => setStatus(e.target.value)}>
                        <option value="PENDING">Pending</option>
                        <option value="APPROVED">Approved</option>
                        <option value="SCHEDULED">Scheduled</option>
                        <option value="REJECTED">Rejected</option>
                        <option value="COMPLETED">Completed</option>
                        <option value="CANCELLED">Cancelled</option>
                    </select>
                </div>

                {status === 'SCHEDULED' && (
                    <div className={styles.inputGroup}>
                        <label>Scheduled Date and Time</label>
                        <input
                            type="datetime-local"
                            value={pickupDate}
                            onChange={e => setPickupDate(e.target.value)}
                            className={styles.scheduledDateTime}
                        />
                    </div>
                )}
                
                {status === 'REJECTED' && (
                    <div className={styles.inputGroup}>
                        <label>Reason for Rejection</label>
                        <textarea value={rejectionReason} onChange={(e) => setRejectionReason(e.target.value)} />
                    </div>
                )}

                {/* Pickup Person Assignment Section */}
                <div className={styles.inputGroup}>
                    <label>Assign Pickup Person</label>
                    <div className={styles.pickupPersonSection}>
                        <select 
                            value={selectedPickupPerson} 
                            onChange={(e) => setSelectedPickupPerson(e.target.value)}
                            className={styles.pickupPersonSelect}
                        >
                            <option value="">Select Pickup Person</option>
                            {pickupPersons.map(person => (
                                <option key={person.id} value={person.id}>
                                    {person.fullName} - {person.email}
                                </option>
                            ))}
                        </select>
                        <button 
                            onClick={handleAssignPickupPerson}
                            className={styles.assignButton}
                            disabled={!selectedPickupPerson}
                        >
                            Assign
                        </button>
                    </div>
                    {request.pickupPersonName && (
                        <div className={styles.currentAssignment}>
                            <strong>Currently assigned to:</strong> {request.pickupPersonName}
                        </div>
                    )}
                </div>

                <div className={styles.modalActions}>
                    <button onClick={handleSubmit} className={styles.updateButton}>Update</button>
                    <button onClick={onClose}>Cancel</button>
                </div>
            </div>
        </div>
    );
};


// --- THIS IS THE MAIN COMPONENT, NOW COMPLETE ---
const AdminDashboard = () => {
    // --- THIS STATE LOGIC WAS MISSING ---
    const [stats, setStats] = useState(null);
    const [requests, setRequests] = useState([]);
    const [users, setUsers] = useState([]);
    const [pickupPersons, setPickupPersons] = useState([]);
    const [view, setView] = useState('dashboard');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [selectedRequest, setSelectedRequest] = useState(null); // For the modal
    const [showHistoryModal, setShowHistoryModal] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);
    const [userHistory, setUserHistory] = useState([]);
    const [loadingHistory, setLoadingHistory] = useState(false);
    const [showPickupPersonModal, setShowPickupPersonModal] = useState(false);

    // --- THIS DATA FETCHING LOGIC WAS MISSING ---
    const fetchData = async () => {
        setLoading(true);
        setError('');
        try {
            const [statsRes, requestsRes, usersRes, pickupPersonsRes] = await Promise.all([
                api.get('/admin/stats'),
                api.get('/admin/requests'),
                api.get('/admin/users'),
                api.get('/admin/pickup-persons')
            ]);
            setStats(statsRes.data);
            setRequests(requestsRes.data);
            setUsers(usersRes.data);
            setPickupPersons(pickupPersonsRes.data);
        } catch (err) {
            console.error("Failed to fetch admin data", err);
            setError("Could not load admin data. Please try again later.");
        } finally {
            setLoading(false);
        }
    };
    
    useEffect(() => {
        fetchData();
    }, []);

    // --- THIS UPDATE HANDLER LOGIC WAS MISSING ---
    const handleStatusUpdate = async (requestId, status, pickupDate, rejectionReason) => {
        try {
            await api.put(`/admin/requests/${requestId}/status`, { status, pickupDate, rejectionReason });
            setSelectedRequest(null); // Close modal on success
            fetchData(); // Refresh all data
        } catch (error) {
            console.error("Failed to update status", error);
            alert("Could not update status.");
        }
    };

    // Fetch user history
    const fetchUserHistory = async (userId, userName) => {
        try {
            setLoadingHistory(true);
            const response = await api.get(`/admin/users/${userId}/requests`);
            setUserHistory(response.data);
            setSelectedUser({ id: userId, name: userName });
            setShowHistoryModal(true);
        } catch (error) {
            console.error("Failed to fetch user history", error);
            alert("Could not load user history.");
        } finally {
            setLoadingHistory(false);
        }
    };

    // Register pickup person
    const handlePickupPersonRegistration = async (formData) => {
        try {
            await api.post('/admin/pickup-persons/register', formData);
            setShowPickupPersonModal(false);
            fetchData(); // Refresh all data
            alert('Pickup person registered successfully!');
        } catch (error) {
            console.error("Failed to register pickup person", error);
            alert("Could not register pickup person. Please try again.");
        }
    };

    // Assign pickup person to request
    const handleAssignPickupPerson = async (requestId, pickupPersonId, scheduledDateTime) => {
        try {
            await api.put(`/admin/requests/${requestId}/assign-pickup-person?pickupPersonId=${pickupPersonId}&scheduledDateTime=${encodeURIComponent(scheduledDateTime)}`);
            fetchData(); // Refresh all data
            alert('Pickup person assigned successfully!');
        } catch (error) {
            console.error("Failed to assign pickup person", error);
            alert("Could not assign pickup person. Please try again.");
        }
    };

    if (loading) return <div className={styles.loading}>Loading Admin Panel...</div>;
    if (error) return <div className={styles.error}>{error}</div>;

    const renderContent = () => {
        switch (view) {
            case 'users':
                return (
                    <div className={styles.tableContainer}>
                        <h2>Manage Users</h2>
                        <table>
                            <thead>
                                <tr><th>ID</th><th>Full Name</th><th>Email</th><th>Address</th><th>Role</th></tr>
                            </thead>
                            <tbody>
                                {users.map(user => (
                                    <tr key={user.id}>
                                        <td>{user.id}</td>
                                        <td>{user.fullName}</td>
                                        <td>{user.email}</td>
                                        <td>{user.address}</td>
                                        <td><span className={user.role === 'ROLE_ADMIN' ? styles.adminRole : styles.userRole}>{user.role.replace('ROLE_', '')}</span></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                );
            case 'pickup-persons':
                return (
                    <div className={styles.tableContainer}>
                        <div className={styles.sectionHeader}>
                            <h2>Manage Pickup Persons</h2>
                            <button 
                                className={styles.addButton}
                                onClick={() => setShowPickupPersonModal(true)}
                            >
                                Add New Pickup Person
                            </button>
                        </div>
                        <table>
                            <thead>
                                <tr><th>ID</th><th>Full Name</th><th>Email</th><th>Mobile</th><th>Address</th></tr>
                            </thead>
                            <tbody>
                                {pickupPersons.map(person => (
                                    <tr key={person.id}>
                                        <td>{person.id}</td>
                                        <td>{person.fullName}</td>
                                        <td>{person.email}</td>
                                        <td>{person.mobileNumber}</td>
                                        <td>{person.address}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                );
            case 'requests':
                return (
                    <div className={styles.requestsContainer}>
                        <h2>Manage Collection Requests</h2>
                        <div className={styles.requestsGrid}>
                            {requests.map(req => (
                                <div key={req.id} className={styles.requestCard}>
                                    <div className={styles.cardHeader}>
                                        <div className={styles.requestId}>#{req.id}</div>
                                        <span className={`${styles.statusBadge} ${styles[req.status.toLowerCase()]}`}>
                                            {req.status}
                                        </span>
                                    </div>
                                    
                                    <div className={styles.cardBody}>
                                        <div className={styles.itemInfo}>
                                            <h3>{req.brand} {req.model}</h3>
                                            <p className={styles.deviceType}>{req.deviceType}</p>
                                        </div>
                                        
                                        <div className={styles.userInfo}>
                                            <div className={styles.userItem}>
                                                <span className={styles.userLabel}>User:</span>
                                                <span>{req.userFullName || 'N/A'}</span>
                                            </div>
                                            <div className={styles.userItem}>
                                                <span className={styles.userLabel}>Quantity:</span>
                                                <span>{req.quantity}</span>
                                            </div>
                                            <div className={styles.userItem}>
                                                <span className={styles.userLabel}>Address:</span>
                                                <span>{req.pickupAddress || 'Not specified'}</span>
                                            </div>
                                            {req.pickupPersonName && (
                                                <div className={styles.userItem}>
                                                    <span className={styles.userLabel}>Assigned to:</span>
                                                    <span className={styles.assignedPerson}>{req.pickupPersonName}</span>
                                                </div>
                                            )}
                                        </div>
                                        
                                        {req.remarks && (
                                            <div className={styles.remarks}>
                                                <strong>Remarks:</strong> {req.remarks}
                                            </div>
                                        )}
                                        
                                        {req.imagePaths && Array.isArray(req.imagePaths) && req.imagePaths.length > 0 && (
                                            <div className={styles.imagePreview}>
                                                <h4>Images:</h4>
                                                <div className={styles.imageThumbnails}>
                                                    {req.imagePaths.map((imagePath, index) => (
                                                        <img
                                                            key={index}
                                                            src={`http://localhost:8080/uploads/${imagePath}`}
                                                            alt={`Request ${req.id} - ${index + 1}`}
                                                            className={styles.thumbnail}
                                                            onError={(e) => {
                                                                e.target.style.display = 'none';
                                                            }}
                                                        />
                                                    ))}
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                    
                                    <div className={styles.cardActions}>
                                        <button 
                                            className={styles.historyButton}
                                            onClick={() => fetchUserHistory(req.userId, req.userFullName)}
                                        >
                                            User History
                                        </button>
                                        <button 
                                            className={styles.editButton} 
                                            onClick={() => setSelectedRequest(req)}
                                        >
                                            Edit Status
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                );
            default: // dashboard view
                return (
                    <div className={styles.statsGrid}>
                        <StatCard icon={<FiUsers />} title="Total Users" value={stats?.totalUsers ?? 0} />
                        <StatCard icon={<FiClipboard />} title="Total Requests" value={stats?.totalRequests ?? 0} />
                        <StatCard icon={<FiLoader />} title="Pending Requests" value={stats?.pendingRequests ?? 0} />
                        <StatCard icon={<FiCheckCircle />} title="Completed Requests" value={stats?.completedRequests ?? 0} />
                    </div>
                );
        }
    };

    return (
        <div className={styles.adminContainer}>
            {selectedRequest && <UpdateModal request={selectedRequest} onClose={() => setSelectedRequest(null)} onUpdate={handleStatusUpdate} onAssignPickupPerson={handleAssignPickupPerson} pickupPersons={pickupPersons} />}
            {showPickupPersonModal && <PickupPersonModal onClose={() => setShowPickupPersonModal(false)} onRegister={handlePickupPersonRegistration} />}
            
            <aside className={styles.sidebar}>
                <h2>Admin Panel</h2>
                <nav>
                    <button onClick={() => setView('dashboard')} className={view === 'dashboard' ? styles.active : ''}><FiGrid /> Dashboard</button>
                    <button onClick={() => setView('requests')} className={view === 'requests' ? styles.active : ''}><FiList /> Manage Requests</button>
                    <button onClick={() => setView('users')} className={view === 'users' ? styles.active : ''}><FiUsers /> Manage Users</button>
                    <button onClick={() => setView('pickup-persons')} className={view === 'pickup-persons' ? styles.active : ''}><FiUsers /> Pickup Persons</button>
                </nav>
            </aside>
            <main className={styles.mainContent}>
                {renderContent()}
            </main>
            
            {/* User History Modal */}
            {showHistoryModal && (
                <div className={styles.modalBackdrop}>
                    <div className={styles.historyModalContent}>
                        <div className={styles.modalHeader}>
                            <h3>User History - {selectedUser?.name}</h3>
                            <button 
                                className={styles.closeButton}
                                onClick={() => setShowHistoryModal(false)}
                            >
                                ×
                            </button>
                        </div>
                        
                        <div className={styles.historyContent}>
                            {loadingHistory ? (
                                <div className={styles.loadingHistory}>Loading user history...</div>
                            ) : (
                                <div className={styles.historyList}>
                                    {userHistory.map((request) => (
                                        <div key={request.id} className={styles.historyCard}>
                                            <div className={styles.historyCardHeader}>
                                                <span className={styles.historyRequestId}>#{request.id}</span>
                                                <span className={`${styles.badge} ${styles[request.status.toLowerCase()]}`}>
                                                    {request.status}
                                                </span>
                                            </div>
                                            <div className={styles.historyCardBody}>
                                                <h4>{request.brand} {request.model}</h4>
                                                <p>{request.deviceType}</p>
                                                <p className={styles.historyDate}>
                                                    {new Date(request.createdAt).toLocaleDateString()}
                                                </p>
                                                {request.imagePaths && Array.isArray(request.imagePaths) && request.imagePaths.length > 0 && (
                                                    <div className={styles.historyImages}>
                                                        {request.imagePaths.map((imagePath, index) => (
                                                            <img
                                                                key={index}
                                                                src={`http://localhost:8080/uploads/${imagePath}`}
                                                                alt={`History ${request.id} - ${index + 1}`}
                                                                className={styles.historyThumbnail}
                                                                onError={(e) => {
                                                                    e.target.style.display = 'none';
                                                                }}
                                                            />
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

const StatCard = ({ icon, title, value }) => (
    <div className={styles.statCard}>
        <div className={styles.cardIcon}>{icon}</div>
        <div className={styles.cardInfo}>
            <span className={styles.cardTitle}>{title}</span>
            <span className={styles.cardValue}>{value}</span>
        </div>
    </div>
);

export default AdminDashboard;