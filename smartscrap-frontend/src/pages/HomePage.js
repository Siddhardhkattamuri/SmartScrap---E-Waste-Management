import React from 'react';
import { motion } from 'framer-motion';
import { FiCpu, FiSmartphone, FiMonitor } from 'react-icons/fi';
import styles from './HomePage.module.css';
import { Link } from 'react-router-dom';

const HomePage = () => {
  return (
    <div className={styles.heroSection}>
      <motion.h1
        initial={{ opacity: 0, y: -50 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8 }}
        className={styles.title}
      >
        Give Your E-Waste a Second Life with <span className={styles.titleHighlight}>SmartScrap</span>.
      </motion.h1>

      <motion.p
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8, delay: 0.2 }}
        className={styles.subtitle}
      >
        Effortlessly schedule pickups for your old electronics. We ensure safe, responsible recycling for a cleaner planet.
      </motion.p>

      <motion.div
        initial={{ opacity: 0, scale: 0.8 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.5, delay: 0.4 }}
      >
        <Link to="/register" className={styles.ctaButton}>
          Schedule a Pickup Now
        </Link>
      </motion.div>

      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.8, duration: 1 }}
        className={styles.iconContainer}
      >
        <FiCpu size={40} />
        <FiSmartphone size={40} />
        <FiMonitor size={40} />
      </motion.div>
    </div>
  );
};

export default HomePage;