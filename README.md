# ♻️ SmartScrap: E-Waste Management System  

A **full-stack web application** designed to streamline the process of **e-waste collection and management**.  
SmartScrap features a secure REST API built with *Spring Boot* ⚙️ and a modern, responsive frontend built with *React* ⚛️.  
The system provides **role-based access** for both users 👤 and administrators 🛠️ to manage the entire lifecycle of e-waste pickup requests.  

---

## ✨ Features  

- 🔐 **Secure JWT Authentication:** End-to-end secure registration and login flow.  
- 🛡️ **Role-Based Access Control:** Separate dashboards for **ROLE_USER** and **ROLE_ADMIN**.  
- 📊 **Dynamic User Dashboard:**  
  - 📝 Submit detailed pickup requests (device type, brand, model, condition, quantity).  
  - 📷 Upload **multiple images** per request.  
  - 🔄 Track live request statuses (**Pending, Approved, Scheduled, Rejected**).  
  - 🕒 View scheduled pickup times or rejection reasons.  
  - 👤 Manage and update profile information.  
- 🖥️ **Comprehensive Admin Dashboard:**  
  - 📈 View system-wide statistics.  
  - ✏️ Manage requests (approve, schedule, reject with reason).  
  - 👥 Access a complete list of registered users.  
- 📱 **Responsive UI:** Clean, modern design with *CSS Modules* ensuring device adaptability.  

---

## 🛠 Tech Stack  

**Backend** ⚙️  
- ☕ Java 17  
- 🌱 Spring Boot 3 + Spring Security (JWT)  
- 🗄️ Spring Data JPA + Hibernate  
- 📦 Maven  

**Frontend** 💻  
- ⚛️ React 18  
- 🛤️ React Router  
- 🔗 Axios (API communication)  
- 🎨 CSS Modules + React Icons  

**Database** 🗃️  
- 🐬 MySQL  

---

## 🚀 Getting Started  

### 📋 Prerequisites  
Ensure the following are installed:  
- ☕ JDK 17+  
- 📦 Apache Maven  
- 💻 Node.js & npm (or yarn)  
- 🐬 MySQL Server  

---

### 1️⃣ Backend Setup  

1. 📂 **Clone Repository:**  
   ```bash
   git clone https://github.com/your-username/your-repo-name.git
   cd your-repo-name
2. 🗃️ Create Database:
   CREATE DATABASE smartscrap_db;
3. ⚙️ Configure Properties:
    Edit smartscrap-backend/src/main/resources/application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/smartscrap_db
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   spring.jpa.hibernate.ddl-auto=update

   # Default Admin
   smartscrap.app.admin.fullName=Admin User
   smartscrap.app.admin.email=ramakrishnakattamuri564@gmail.com
   smartscrap.app.admin.password=adminpassword123
4. ▶️ Run Backend:
   cd smartscrap-backend
   ./mvnw spring-boot:run

