import React from 'react';
import { NavLink } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import { FaRecycle } from 'react-icons/fa';
import styles from './Navbar.module.css';

const Navbar = () => {
  const { user, logout } = useAuth();

  return (
    <nav className={styles.navbar}>
      <div className={styles.container}>
        <NavLink to="/" className={styles.logo}>
          <FaRecycle className={styles.logoIcon} />
          <span>SmartScrap</span>
        </NavLink>
        <div className={styles.navLinks}>
          <NavLink to="/" className={({ isActive }) => (isActive ? `${styles.link} ${styles.active}` : styles.link)}>
            Home
          </NavLink>
          {user ? (
            <>
              <NavLink to="/dashboard" className={({ isActive }) => (isActive ? `${styles.link} ${styles.active}` : styles.link)}>
                Dashboard
              </NavLink>
              <button onClick={logout} className={`${styles.navButton} ${styles.logoutButton}`}>
                Logout
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={styles.link}>
                Login
              </NavLink>
              <NavLink to="/register" className={`${styles.navButton} ${styles.registerButton}`}>
                Register
              </NavLink>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;