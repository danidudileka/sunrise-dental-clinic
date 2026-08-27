package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.dao.PatientDao;
import com.sunrisedental.dto.request.PatientRequest;
import com.sunrisedental.dto.response.AppointmentResponse;
import com.sunrisedental.dto.response.PatientResponse;
import com.sunrisedental.exception.NotFoundException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Patient;
import com.sunrisedental.util.DateUtil;
import com.sunrisedental.util.EmailUtil;
import com.sunrisedental.util.ValidationUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for patient operations.
 * Handles patient registration and management.
 */
public class PatientService {
    private static final Logger logger = LogManager.getLogger(PatientService.class);
    private final PatientDao patientDao;
    private final AppointmentDao appointmentDao;

    public PatientService() {
        this.patientDao = new PatientDao();
        this.appointmentDao = new AppointmentDao();
    }

    /**
     * Register new patient
     */
    public PatientResponse registerPatient(PatientRequest request) {
        // Validate request
        validatePatientRequest(request);

        // Check for existing patient
        Optional<Patient> existingPatient = patientDao.findByContactNumber(request.getContactNumber());
        if (existingPatient.isPresent()) {
            throw new ValidationException("Patient with this contact number already exists. Patient ID: " +
                    existingPatient.get().getPatientCode());
        }

        // Generate patient code
        String patientCode = patientDao.generateNextPatientCode();

        // Create new patient
        Patient patient = Patient.builder()
                .patientCode(patientCode)
                .patientName(request.getPatientName())
                .address(request.getAddress())
                .contactNumber(request.getContactNumber())
                .email(request.getEmail())
                .dateOfBirth(DateUtil.parseDate(request.getDateOfBirth()))
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .medicalHistory(request.getMedicalHistory())
                .build();

        int patientId = patientDao.createPatient(patient);
        logger.info("Created new patient with ID: {} and Code: {}", patientId, patientCode);

        // Send email if provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            try {
                EmailUtil.sendPatientRegistrationEmail(
                        request.getEmail(),
                        request.getPatientName(),
                        patientCode
                );
            } catch (Exception e) {
                logger.error("Error sending registration email", e);
            }
        }

        // Return patient response
        return getPatientByCode(patientCode);
    }

    /**
     * Get patient by code
     */
    public PatientResponse getPatientByCode(String patientCode) {
        Optional<Patient> patientOptional = patientDao.findByPatientCode(patientCode);

        if (patientOptional.isEmpty()) {
            throw new NotFoundException("Patient", patientCode);
        }

        return mapToPatientResponse(patientOptional.get());
    }

    /**
     * Get patient by ID
     */
    public PatientResponse getPatientById(int patientId) {
        Optional<Patient> patientOptional = patientDao.findById(patientId);

        if (patientOptional.isEmpty()) {
            throw new NotFoundException("Patient", String.valueOf(patientId));
        }

        return mapToPatientResponse(patientOptional.get());
    }

    /**
     * Get patient by contact number
     */
    public PatientResponse getPatientByContactNumber(String contactNumber) {
        Optional<Patient> patientOptional = patientDao.findByContactNumber(contactNumber);

        if (patientOptional.isEmpty()) {
            throw new NotFoundException("Patient", contactNumber);
        }

        return mapToPatientResponse(patientOptional.get());
    }

    /**
     * Search patients by name
     */
    public List<PatientResponse> searchPatientsByName(String name) {
        if (name == null || name.trim().length() < 2) {
            throw new ValidationException("Search term must be at least 2 characters");
        }

        List<Patient> patients = patientDao.findByName(name.trim());
        return patients.stream()
                .map(this::mapToPatientResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all active patients
     */
    public List<PatientResponse> getAllPatients() {
        List<Patient> patients = patientDao.findAllActive();
        return patients.stream()
                .map(this::mapToPatientResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get patient with appointment history
     */
    public Map<String, Object> getPatientWithAppointments(String patientCode) {
        PatientResponse patient = getPatientByCode(patientCode);

        // Get patient ID from response
        Optional<Patient> patientOptional = patientDao.findByPatientCode(patientCode);
        if (patientOptional.isEmpty()) {
            throw new NotFoundException("Patient", patientCode);
        }

        List<Appointment> appointments = appointmentDao.findByPatientId(patientOptional.get().getPatientId());

        List<AppointmentResponse> appointmentResponses = appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("patient", patient);
        result.put("appointments", appointmentResponses);

        return result;
    }

    /**
     * Update patient information
     */
    public PatientResponse updatePatient(int patientId, PatientRequest request) {
        Optional<Patient> patientOptional = patientDao.findById(patientId);

        if (patientOptional.isEmpty()) {
            throw new NotFoundException("Patient", String.valueOf(patientId));
        }

        validatePatientRequest(request);

        Patient patient = patientOptional.get();
        patient.setPatientName(request.getPatientName());
        patient.setAddress(request.getAddress());
        patient.setContactNumber(request.getContactNumber());
        patient.setEmail(request.getEmail());
        patient.setDateOfBirth(DateUtil.parseDate(request.getDateOfBirth()));
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setMedicalHistory(request.getMedicalHistory());

        boolean updated = patientDao.updatePatient(patient);

        if (updated) {
            logger.info("Updated patient with ID: {}", patientId);
            return getPatientById(patientId);
        }

        throw new ValidationException("Failed to update patient");
    }

    /**
     * Deactivate patient
     */
    public boolean deactivatePatient(int patientId) {
        Optional<Patient> patientOptional = patientDao.findById(patientId);

        if (patientOptional.isEmpty()) {
            throw new NotFoundException("Patient", String.valueOf(patientId));
        }

        boolean deactivated = patientDao.deactivatePatient(patientId);

        if (deactivated) {
            logger.info("Deactivated patient with ID: {}", patientId);
        }

        return deactivated;
    }

    /**
     * Validate patient request
     */
    private void validatePatientRequest(PatientRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request == null) {
            throw new ValidationException("Patient request cannot be null");
        }

        if (request.getPatientName() == null || !ValidationUtil.isValidName(request.getPatientName())) {
            errors.put("patientName", "Valid patient name is required");
        }

        if (request.getAddress() != null && !ValidationUtil.isValidAddress(request.getAddress())) {
            errors.put("address", "Address must be at least 5 characters");
        }

        if (request.getContactNumber() == null || !ValidationUtil.isValidPhoneNumber(request.getContactNumber())) {
            errors.put("contactNumber", "Valid contact number is required");
        }

        if (request.getEmail() != null && !ValidationUtil.isValidEmail(request.getEmail())) {
            errors.put("email", "Invalid email address");
        }

        if (request.getDateOfBirth() != null && !ValidationUtil.isValidDate(request.getDateOfBirth())) {
            errors.put("dateOfBirth", "Invalid date of birth format");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    /**
     * Map Patient to PatientResponse
     */
    private PatientResponse mapToPatientResponse(Patient patient) {
        return PatientResponse.builder()
                .patientId(patient.getPatientId())
                .patientCode(patient.getPatientCode())
                .patientName(patient.getPatientName())
                .address(patient.getAddress())
                .contactNumber(patient.getContactNumber())
                .email(patient.getEmail())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .bloodGroup(patient.getBloodGroup())
                .medicalHistory(patient.getMedicalHistory())
                .createdAt(patient.getCreatedAt())
                .build();
    }

    /**
     * Map Appointment to AppointmentResponse
     */
    private AppointmentResponse mapToAppointmentResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .appointmentId(appointment.getAppointmentId())
                .appointmentNumber(appointment.getAppointmentNumber())
                .patientName(appointment.getPatientName())
                .contactNumber(appointment.getPatientContactNumber())
                .dentistName(appointment.getDentistName())
                .treatmentType(appointment.getTreatmentName())
                .treatmentCost(appointment.getTreatmentCost())
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .consultationFee(appointment.getConsultationFee())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}