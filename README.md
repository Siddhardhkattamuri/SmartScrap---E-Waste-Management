# SmartScrap: E-Waste Management System

A full-stack web application designed to streamline the process of e-waste collection and management. This project features a secure REST API built with *Spring Boot* and a dynamic, responsive frontend built with *React*. The system provides role-based access for users and administrators to manage the entire lifecycle of an e-waste pickup request.

## ✨ Features

- *Secure JWT Authentication:* End-to-end secure registration and login flow using JSON Web Tokens.
- *Role-Based Access Control:* Distinct dashboards and permissions for ROLE_USER and ROLE_ADMIN roles, ensuring a secure separation of concerns.
- *Dynamic User Dashboard:* A feature-rich interface where users can:
  - Submit detailed e-waste pickup requests with fields for device type, brand, model, condition, and quantity.
  - *Upload multiple images* for each request.
  - Track the live status of their submissions (Pending, Approved, Scheduled, Rejected).
  - View important feedback, such as a *scheduled pickup time* or a *reason for rejection*.
  - View and update their own profile information.
- *Comprehensive Admin Dashboard:* A protected panel where administrators can:
  - View system-wide statistics at a glance.
  - Manage all user requests via an interactive modal to update statuses, set pickup times, or provide rejection reasons.
  - View a complete list of all registered users in the system.
- *Responsive UI:* A clean and modern user interface built with *CSS Modules* for component-scoped styling, ensuring a great experience on any device.

## 🛠 Tech Stack

- *Backend:*
  - Java 17
  - Spring Boot 3
  - Spring Security (with JWT)
  - Spring Data JPA & Hibernate
  - Maven
- *Frontend:*
  - React 18
  - React Router
  - Axios (for API communication)
  - CSS Modules
  - React Icons
- *Database:*
  - MySQL

---

## 🚀 Getting Started

Follow these instructions to get the project up and running on your local machine for development and testing.

### Prerequisites

You will need the following software installed on your machine:
- JDK 17 or newer (Java Development Kit)
- Apache Maven
- Node.js and npm (or yarn)
- MySQL Server

### 1. Backend Setup

First, let's get the Spring Boot server running.

1.  *Clone the repository (if you haven't already):*
    bash
    git clone https://github.com/your-username/your-repo-name.git
    cd your-repo-name
    
2.  *Create the Database:*
    Open your MySQL client and run the following command to create the database:
    sql
    CREATE DATABASE smartscrap_db;
    
3.  *Configure Application Properties:*
    Navigate to the backend properties file:
    
    smartscrap-backend/src/main/resources/application.properties
    
    - Update the spring.datasource.password with your MySQL root password.
    - Configure the default admin account that will be created on the first run.
    properties
    # Database
    spring.datasource.url=jdbc:mysql://localhost:3306/smartscrap_db
    spring.datasource.username=root
    spring.datasource.password=your_mysql_password
    spring.jpa.hibernate.ddl-auto=update

    # Default Admin Account
    smartscrap.app.admin.fullName=Admin User
    smartscrap.app.admin.email=ramakrishnakattamuri564@gmail.com
    smartscrap.app.admin.password=adminpassword123
    
4.  *Run the Backend Server:*
    Navigate to the backend's root folder and run the application using Maven:
    bash
    cd smartscrap-backend
    ./mvnw spring-boot:run
    
    The backend server will start on http://localhost:8080.

### 2. Frontend Setup

Now, let's get the React user interface running.

1.  *Navigate to the frontend folder:*
    Open a *new terminal* and navigate to the frontend directory.
    bash
    cd smartscrap-frontend
    
2.  *Install Dependencies:*
    bash
    npm install
    
3.  *Run the Frontend App:*
    bash
    npm start
    
    The React development server will start, and the application will open in your browser at http://localhost:3000.

---

## 📖 Application Usage

1.  *Register a User:* Navigate to the registration page (/register) and create a new user account with all the required details.
2.  *Log In as a User:* Use the credentials of the new user to log in. You will be redirected to the user dashboard where you can submit and track requests.
3.  *Log In as an Admin:*
    - The admin account is automatically created on the first backend startup using the credentials you set in application.properties.
    - Log out from the user account.
    - Log in using the admin email and password (e.g., ramakrishnakattamuri564@gmail.com / adminpassword123).
    - You will be redirected to the admin dashboard with full control over users and requests.
