# ERP System (Spring Boot Backend)

This is the **backend** of a role-based ERP (Enterprise Resource Planning) system developed using **Spring Boot** and **MySQL**. The system supports modules like **Admin**, **HR**, **Finance**, and **Inventory**, with secure JWT authentication and role-based access control.

## Features

### 1. **Authentication & Security**
- JWT-based login and signup
- Role-based access control (Admin, HR, Finance, Inventory)
- Password encryption using Spring Security

### 2. **Admin Module**
- Create/Edit/Delete users
- Assign roles (HR, Finance, Inventory)
- Activate/Deactivate users
- Reset user passwords
- View user roles and permissions

### 3. **Department Management**
- Create/Edit/Delete departments
- Assign users to departments
- Department-wise user listing

### 4. **HR Module**
- Employee CRUD operations
- Leave Management (Request/Approve/Reject)
- Attendance Management with enum statuses:
  - PRESENT
  - ABSENT
  - LEAVE
  - NOT_MARKED
- Auto-leave attendance update
- Employee performance and payroll management

### 5. **Inventory Module**
- Product stock management
- Purchase order tracking

### 6. **Finance Module**
- Salary and payroll processing
- Financial reporting (planned)

## Technologies Used
- Java 17+
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- JWT (Authentication)
- Lombok
- Maven

## Folder Structure

src/ ├── config/          → Security & JWT configuration
├── controller/      → REST API controllers
├── dto/             → Data Transfer Objects
├── entity/          → JPA Entities
├── repository/      → Spring Data Repositories
├── request/         → Incoming request models
├── response/        → API responses
├── service/         → Business logic
└── service/impl/    → Service implementations

## Getting Started

### Prerequisites
- Java 17+
- MySQL
- Maven

### Setup
1. Clone the repo:
   ```bash
   git clone https://github.com/swapnilshinde1101/ERP.git
 ```
2. Create a MySQL database:

   CREATE DATABASE erp_system;


3. Configure DB credentials in application.properties.

4. Run the Spring Boot application:

   mvn spring-boot:run

