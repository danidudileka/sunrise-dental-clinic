# Sunrise Dental Clinic Management System

![Build and Test](https://github.com/danidudileka/sunrise-dental-clinic/actions/workflows/build.yml/badge.svg)

A web-based appointment and patient management system built for Sunrise Dental Clinic, Colombo — replacing manual, paper-based record keeping with a secure, role-based digital workflow covering patient registration, appointment scheduling, billing, and reporting.

<img width="1920" height="917" alt="login" src="https://github.com/user-attachments/assets/a5e05d47-b4cb-4a32-a1c2-36219244d9df" />


## Features

- **Secure Authentication** — BCrypt password hashing, session management, and account lockout after 5 failed login attempts
- **Patient Management** — Registration with auto-generated patient IDs, multi-criteria search, and medical history tracking
- **Appointment Scheduling** — Conflict/double-booking detection and unique appointment number generation
- **Automated Billing** — Consultation fee + treatment cost calculation, automatic discounting, and receipt generation
- **Role-Based Access Control** — Separate views and permissions for Admin, Receptionist, and Dentist roles
- **Reporting & Analytics** — Daily appointment reports, revenue analysis, and dentist performance dashboards
- **Email Notifications** — Automated confirmation emails for registrations and appointments

## Tech Stack

| Layer        | Technology                      |
| ------------ | ------------------------------- |
| Frontend     | HTML5, CSS3, Vanilla JavaScript |
| Backend      | Jakarta Servlets (Java 23)      |
| Database     | MySQL 8.x                       |
| Connectivity | JDBC (PreparedStatement)        |
| Build Tool   | Maven                           |
| Server       | Apache Tomcat 11                |
| Testing      | JUnit 5, Mockito                |
| Security     | BCrypt, Session Management      |

Built on a **three-tier architecture** (presentation, business, data) using DAO, DTO, MVC, Singleton, Factory, and Filter design patterns.

## Getting Started

### Prerequisites

- JDK 23
- Apache Maven
- MySQL 8.x
- Apache Tomcat 11

### Setup

1. Clone the repository
   ```bash
   git clone https://github.com/danidudileka/sunrise-dental-clinic.git
   ```
2. Create the MySQL database and import the schema/seed data from `/database`
3. Update database credentials in `src/main/resources/config` (or equivalent config file)
4. Build the project
   ```bash
   mvn clean package
   ```
5. Deploy the generated `.war` file (from `target/`) to Apache Tomcat 11
6. Access the application at `http://localhost:8080/sunrise-dental-clinic`

## Running Tests

```bash
mvn test
```

Includes unit tests for authentication, patient, appointment, and billing services, plus validation and password utilities (54 automated tests).

## CI/CD

Every push and pull request to `master` triggers a GitHub Actions workflow that builds the project, runs the full test suite, and uploads the packaged WAR file as a build artifact. See `.github/workflows/build.yml`.

## Project Structure

```
src/main/java/
├── config/       # App configuration & DB connection (Singleton)
├── controller/   # Servlet controllers (REST endpoints)
├── service/      # Business logic
├── dao/          # Data access layer
├── model/        # Entity classes
├── dto/          # Request/response data transfer objects
├── exception/    # Custom exception classes
├── filter/       # Auth, CORS, and logging filters
└── util/         # Password, validation, email, date utilities
src/main/webapp/  # Frontend HTML, CSS, JS
src/test/         # JUnit/Mockito test suites
```

## User Roles

| Role         | Access                                          |
| ------------ | ----------------------------------------------- |
| Admin        | Full access, including staff account management |
| Receptionist | Patients, appointments, billing, reports        |
| Dentist      | Own appointments, patients, and revenue only    |

## License

This project was developed for academic purposes as part of a university coursework assignment.
