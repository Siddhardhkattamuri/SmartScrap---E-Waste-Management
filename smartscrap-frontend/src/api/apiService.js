import axios from 'axios';

// Create an Axios instance pre-configured for your Spring Boot API
const api = axios.create({
  baseURL: 'http://localhost:8080/api', // The base URL of your backend
});

// Use an interceptor to automatically add the JWT token to requests
api.interceptors.request.use(
  (config) => {
    // Retrieve the user object from local storage
    const userString = localStorage.getItem('user');
    if (userString) {
      const user = JSON.parse(userString);
      const token = user?.token;
      if (token) {
        // If a token exists, add it to the Authorization header
        config.headers['Authorization'] = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    // Handle request errors
    return Promise.reject(error);
  }
);

export default api;