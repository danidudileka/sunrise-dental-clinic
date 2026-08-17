-- Seed Data for Sunrise Dental Clinic
USE
sunrise_dental_clinic;

-- Users (password: admin123, reception123, dentist123)
INSERT INTO users (username, password_hash, full_name, email, role)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'System Administrator',
        'admin@sunrisedental.com', 'ADMIN'),
       ('reception', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Reception Staff',
        'reception@sunrisedental.com', 'RECEPTIONIST'),
       ('dr.smith', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Dr. John Smith',
        'dr.smith@sunrisedental.com', 'DENTIST');

-- Dentists
INSERT INTO dentists (name, specialization, license_number, phone, email)
VALUES ('Dr. John Smith', 'General Dentistry', 'DLC-2024-001', '+94-11-2345678', 'dr.smith@sunrisedental.com'),
       ('Dr. Sarah Johnson', 'Orthodontics', 'DLC-2024-002', '+94-11-2345679', 'dr.johnson@sunrisedental.com'),
       ('Dr. Michael Brown', 'Periodontics', 'DLC-2024-003', '+94-11-2345680', 'dr.brown@sunrisedental.com'),
       ('Dr. Emily Davis', 'Endodontics', 'DLC-2024-004', '+94-11-2345681', 'dr.davis@sunrisedental.com'),
       ('Dr. David Wilson', 'Oral Surgery', 'DLC-2024-005', '+94-11-2345682', 'dr.wilson@sunrisedental.com');

-- Treatments
INSERT INTO treatments (treatment_code, treatment_name, description, base_cost, duration_minutes)
VALUES ('GEN-CHK', 'General Checkup', 'Routine dental examination and cleaning', 2000.00, 30),
       ('TEETH-CLN', 'Teeth Cleaning', 'Professional dental cleaning and scaling', 3500.00, 45),
       ('TOOTH-EXT', 'Tooth Extraction', 'Simple tooth extraction procedure', 4000.00, 60),
       ('ROOT-CNL', 'Root Canal Treatment', 'Root canal therapy for infected tooth', 15000.00, 120),
       ('FILLING', 'Dental Filling', 'Composite or amalgam filling for cavities', 2500.00, 45),
       ('CROWN', 'Dental Crown', 'Dental crown placement for damaged tooth', 20000.00, 90),
       ('BRIDGE', 'Dental Bridge', 'Fixed bridge for missing teeth', 25000.00, 120),
       ('DENTURE', 'Dentures', 'Full or partial denture fitting', 30000.00, 150),
       ('BRACES', 'Orthodontic Braces', 'Braces for teeth alignment', 50000.00, 180),
       ('WHITENING', 'Teeth Whitening', 'Professional teeth whitening treatment', 8000.00, 60);

-- Sample Patients
INSERT INTO patients (patient_name, address, contact_number, email, date_of_birth, gender)
VALUES ('John Doe', '123 Main Street, Colombo 07', '+94-77-1234567', 'john.doe@gmail.com', '1985-03-15', 'MALE'),
       ('Jane Smith', '456 Galle Road, Colombo 03', '+94-76-2345678', 'jane.smith@yahoo.com', '1990-07-22', 'FEMALE'),
       ('Robert Wilson', '789 Kandy Road, Colombo 10', '+94-75-3456789', 'robert.wilson@gmail.com', '1978-11-30',
        'MALE'),
       ('Mary Johnson', '321 Havelock Road, Colombo 05', '+94-71-4567890', 'mary.johnson@gmail.com', '1988-01-18',
        'FEMALE'),
       ('David Brown', '654 Negombo Road, Colombo 04', '+94-72-5678901', 'david.brown@yahoo.com', '1995-09-05', 'MALE'),
       ('Sarah Davis', '987 Stanley Road, Colombo 08', '+94-70-6789012', 'sarah.davis@gmail.com', '1982-04-12',
        'FEMALE'),
       ('Michael Miller', '159 Park Road, Colombo 02', '+94-77-7890123', 'michael.miller@gmail.com', '1975-08-25',
        'MALE'),
       ('Emily Wilson', '753 Flower Road, Colombo 07', '+94-76-8901234', 'emily.wilson@yahoo.com', '1992-12-08',
        'FEMALE'),
       ('James Taylor', '258 Green Path, Colombo 03', '+94-75-9012345', 'james.taylor@gmail.com', '1987-06-20', 'MALE'),
       ('Emma Anderson', '456 Marine Drive, Colombo 06', '+94-71-0123456', 'emma.anderson@gmail.com', '1993-02-14',
        'FEMALE');

-- Sample Appointments
INSERT INTO appointments (appointment_number, patient_id, dentist_id, treatment_id, appointment_date, appointment_time,
                          status, notes)
VALUES ('APT202400001', 1, 1, 1, '2024-01-15', '09:00:00', 'COMPLETED', 'Regular checkup'),
       ('APT202400002', 2, 2, 2, '2024-01-15', '10:30:00', 'COMPLETED', 'Teeth cleaning session'),
       ('APT202400003', 3, 3, 3, '2024-01-16', '11:00:00', 'SCHEDULED', 'Tooth extraction'),
       ('APT202400004', 4, 4, 4, '2024-01-16', '14:00:00', 'SCHEDULED', 'Root canal treatment'),
       ('APT202400005', 5, 5, 5, '2024-01-17', '09:30:00', 'SCHEDULED', 'Cavity filling'),
       ('APT202400006', 6, 1, 6, '2024-01-17', '11:30:00', 'SCHEDULED', 'Crown placement'),
       ('APT202400007', 7, 2, 7, '2024-01-18', '13:00:00', 'SCHEDULED', 'Bridge fitting'),
       ('APT202400008', 8, 3, 8, '2024-01-18', '15:30:00', 'SCHEDULED', 'Denture fitting'),
       ('APT202400009', 9, 4, 9, '2024-01-19', '10:00:00', 'SCHEDULED', 'Braces installation'),
       ('APT202400010', 10, 5, 10, '2024-01-19', '14:30:00', 'SCHEDULED', 'Teeth whitening');

-- Sample Bills
INSERT INTO bills (appointment_id, treatment_cost, consultation_fee, total_amount, payment_status, payment_method)
VALUES (1, 2000.00, 500.00, 2500.00, 'PAID', 'CASH'),
       (2, 3500.00, 500.00, 4000.00, 'PAID', 'CARD'),
       (3, 4000.00, 500.00, 4500.00, 'PENDING', NULL),
       (4, 15000.00, 500.00, 15500.00, 'PENDING', NULL),
       (5, 2500.00, 500.00, 3000.00, 'PENDING', NULL);