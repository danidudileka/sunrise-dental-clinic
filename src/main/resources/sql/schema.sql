-- Sunrise Dental Clinic Database Schema
-- Create Database
CREATE DATABASE IF NOT EXISTS sunrise_dental_clinic;
USE sunrise_dental_clinic;

-- Drop existing tables (in reverse order of dependencies)
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS treatments;
DROP TABLE IF EXISTS dentists;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS login_attempts;

-- Users Table
CREATE TABLE users (
                       user_id INT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(100),
                       role ENUM('ADMIN', 'RECEPTIONIST', 'DENTIST') NOT NULL DEFAULT 'RECEPTIONIST',
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_username (username),
                       INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Login Attempts Table
CREATE TABLE login_attempts (
                                attempt_id INT PRIMARY KEY AUTO_INCREMENT,
                                username VARCHAR(50) NOT NULL,
                                attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                ip_address VARCHAR(45),
                                success BOOLEAN DEFAULT FALSE,
                                INDEX idx_username_time (username, attempt_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dentists Table
CREATE TABLE dentists (
                          dentist_id INT PRIMARY KEY AUTO_INCREMENT,
                          name VARCHAR(100) NOT NULL,
                          specialization VARCHAR(100),
                          license_number VARCHAR(50) UNIQUE,
                          phone VARCHAR(20),
                          email VARCHAR(100),
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          INDEX idx_dentist_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Treatments Table
CREATE TABLE treatments (
                            treatment_id INT PRIMARY KEY AUTO_INCREMENT,
                            treatment_code VARCHAR(20) NOT NULL UNIQUE,
                            treatment_name VARCHAR(100) NOT NULL,
                            description TEXT,
                            base_cost DECIMAL(10, 2) NOT NULL,
                            duration_minutes INT DEFAULT 30,
                            is_active BOOLEAN DEFAULT TRUE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            INDEX idx_treatment_code (treatment_code),
                            INDEX idx_treatment_name (treatment_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Patients Table
CREATE TABLE patients (
                          patient_id INT PRIMARY KEY AUTO_INCREMENT,
                          patient_name VARCHAR(100) NOT NULL,
                          address TEXT,
                          contact_number VARCHAR(20) NOT NULL,
                          email VARCHAR(100),
                          date_of_birth DATE,
                          gender ENUM('MALE', 'FEMALE', 'OTHER'),
                          blood_group VARCHAR(5),
                          medical_history TEXT,
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          INDEX idx_patient_name (patient_name),
                          INDEX idx_contact_number (contact_number),
                          INDEX idx_patient_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Appointments Table
CREATE TABLE appointments (
                              appointment_id INT PRIMARY KEY AUTO_INCREMENT,
                              appointment_number VARCHAR(20) NOT NULL UNIQUE,
                              patient_id INT NOT NULL,
                              dentist_id INT NOT NULL,
                              treatment_id INT NOT NULL,
                              appointment_date DATE NOT NULL,
                              appointment_time TIME NOT NULL,
                              consultation_fee DECIMAL(10, 2) DEFAULT 500.00,
                              status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW') DEFAULT 'SCHEDULED',
                              notes TEXT,
                              created_by INT,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
                              FOREIGN KEY (dentist_id) REFERENCES dentists(dentist_id) ON DELETE CASCADE,
                              FOREIGN KEY (treatment_id) REFERENCES treatments(treatment_id) ON DELETE CASCADE,
                              FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
                              INDEX idx_appointment_number (appointment_number),
                              INDEX idx_appointment_date (appointment_date),
                              INDEX idx_patient_id (patient_id),
                              INDEX idx_dentist_id (dentist_id),
                              INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bills Table
CREATE TABLE bills (
                       bill_id INT PRIMARY KEY AUTO_INCREMENT,
                       bill_number VARCHAR(20) NOT NULL UNIQUE,
                       appointment_id INT NOT NULL,
                       treatment_cost DECIMAL(10, 2) NOT NULL,
                       consultation_fee DECIMAL(10, 2) NOT NULL,
                       additional_charges DECIMAL(10, 2) DEFAULT 0.00,
                       discount DECIMAL(10, 2) DEFAULT 0.00,
                       total_amount DECIMAL(10, 2) NOT NULL,
                       payment_status ENUM('PENDING', 'PAID', 'PARTIALLY_PAID', 'REFUNDED') DEFAULT 'PENDING',
                       payment_method ENUM('CASH', 'CARD', 'INSURANCE', 'ONLINE'),
                       bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       created_by INT,
                       notes TEXT,
                       FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE CASCADE,
                       FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
                       INDEX idx_bill_number (bill_number),
                       INDEX idx_appointment_id (appointment_id),
                       INDEX idx_payment_status (payment_status),
                       INDEX idx_bill_date (bill_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Stored Procedure: Get Daily Appointments
DELIMITER //
CREATE PROCEDURE GetDailyAppointments(IN p_date DATE)
BEGIN
SELECT
    a.appointment_number,
    a.appointment_time,
    p.patient_name,
    p.contact_number,
    d.name AS dentist_name,
    t.treatment_name,
    a.status
FROM appointments a
         INNER JOIN patients p ON a.patient_id = p.patient_id
         INNER JOIN dentists d ON a.dentist_id = d.dentist_id
         INNER JOIN treatments t ON a.treatment_id = t.treatment_id
WHERE a.appointment_date = p_date
ORDER BY a.appointment_time;
END//

-- Stored Procedure: Calculate Revenue
CREATE PROCEDURE CalculateRevenue(IN p_start_date DATE, IN p_end_date DATE)
BEGIN
SELECT
    DATE(b.bill_date) AS bill_date,
    COUNT(b.bill_id) AS total_bills,
    SUM(b.total_amount) AS total_revenue,
    SUM(b.treatment_cost) AS treatment_revenue,
    SUM(b.consultation_fee) AS consultation_revenue
FROM bills b
WHERE DATE(b.bill_date) BETWEEN p_start_date AND p_end_date
  AND b.payment_status = 'PAID'
GROUP BY DATE(b.bill_date)
ORDER BY bill_date;
END//

-- Trigger: Auto-generate bill number
DELIMITER //
CREATE TRIGGER trg_generate_bill_number
    BEFORE INSERT ON bills
    FOR EACH ROW
BEGIN
    DECLARE next_number INT;
    SELECT COALESCE(MAX(CAST(SUBSTRING(bill_number, 5) AS UNSIGNED)), 0) + 1
    INTO next_number FROM bills;
    SET NEW.bill_number = CONCAT('BILL', LPAD(next_number, 6, '0'));
END//
DELIMITER ;