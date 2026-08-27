package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.dao.DentistDao;
import com.sunrisedental.dao.PatientDao;
import com.sunrisedental.dao.TreatmentDao;
import com.sunrisedental.dto.request.AppointmentRequest;
import com.sunrisedental.dto.response.AppointmentResponse;
import com.sunrisedental.exception.NotFoundException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;
import com.sunrisedental.util.DateUtil;
import com.sunrisedental.util.EmailUtil;
import com.sunrisedental.util.ValidationUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for appointment operations.
 * Implements business logic for appointment management.
 */
public class AppointmentService {
    private static final Logger logger = LogManager.getLogger(AppointmentService.class);
    private final AppointmentDao appointmentDao;
    private final PatientDao patientDao;
    private final DentistDao dentistDao;
    private final TreatmentDao treatmentDao;

    // Business constants
    private static final BigDecimal DEFAULT_CONSULTATION_FEE = new BigDecimal("500.00");

    public AppointmentService() {
        this.appointmentDao = new AppointmentDao();
        this.patientDao = new PatientDao();
        this.dentistDao = new DentistDao();
        this.treatmentDao = new TreatmentDao();
    }

    /**
     * Register new appointment
     */
    public AppointmentResponse registerAppointment(AppointmentRequest request, int createdBy) {
        // Validate request
        validateAppointmentRequest(request);

        // Check if appointment number is unique
        String appointmentNumber = request.getAppointmentNumber();
        if (appointmentNumber == null || appointmentNumber.trim().isEmpty()) {
            appointmentNumber = appointmentDao.generateNextAppointmentNumber();
        } else {
            if (appointmentDao.existsByAppointmentNumber(appointmentNumber)) {
                throw new ValidationException("Appointment number already exists");
            }
        }

        // Get or create patient
        int patientId;
        Optional<Patient> patientOptional = patientDao.findByContactNumber(request.getContactNumber());

        if (patientOptional.isPresent()) {
            patientId = patientOptional.get().getPatientId();
            logger.debug("Using existing patient with ID: {}", patientId);
        } else {
            // Create new patient
            Patient newPatient = Patient.builder()
                    .patientName(request.getPatientName())
                    .address(request.getAddress())
                    .contactNumber(request.getContactNumber())
                    .email(request.getEmail())
                    .build();

            patientId = patientDao.createPatient(newPatient);
            logger.info("Created new patient with ID: {}", patientId);
        }

        // Get dentist
        Optional<Dentist> dentistOptional = dentistDao.findByName(request.getDentistName());
        if (dentistOptional.isEmpty()) {
            throw new ValidationException("Dentist not found: " + request.getDentistName());
        }
        int dentistId = dentistOptional.get().getDentistId();

        // Get treatment
        Optional<Treatment> treatmentOptional = treatmentDao.findByName(request.getTreatmentType());
        if (treatmentOptional.isEmpty()) {
            throw new ValidationException("Treatment not found: " + request.getTreatmentType());
        }
        int treatmentId = treatmentOptional.get().getTreatmentId();

        // Parse date and time
        LocalDate appointmentDate = DateUtil.parseDate(request.getAppointmentDate());
        LocalTime appointmentTime = DateUtil.parseTime(request.getAppointmentTime());

        // Check for double booking
        if (appointmentDao.isDoubleBooked(dentistId, appointmentDate, appointmentTime)) {
            throw new ValidationException("Dentist already has an appointment at this date and time");
        }

        // Create appointment
        Appointment appointment = Appointment.builder()
                .appointmentNumber(appointmentNumber)
                .patientId(patientId)
                .dentistId(dentistId)
                .treatmentId(treatmentId)
                .appointmentDate(appointmentDate)
                .appointmentTime(appointmentTime)
                .consultationFee(request.getConsultationFee() != null ?
                        request.getConsultationFee() : DEFAULT_CONSULTATION_FEE)
                .status("SCHEDULED")
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();


        int appointmentId = appointmentDao.createAppointment(appointment);
        logger.info("Created appointment with ID: {} and number: {}", appointmentId, appointmentNumber);

// Send email confirmation if patient email is provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            try {
                boolean emailSent = EmailUtil.sendAppointmentConfirmation(
                        request.getEmail(),
                        request.getPatientName(),
                        appointmentNumber,
                        request.getDentistName(),
                        request.getTreatmentType(),
                        request.getAppointmentDate(),
                        request.getAppointmentTime()
                );

                if (emailSent) {
                    logger.info("Appointment confirmation email sent to: {}", request.getEmail());
                } else {
                    logger.warn("Failed to send confirmation email to: {}", request.getEmail());
                }
            } catch (Exception e) {
                logger.error("Error sending confirmation email", e);
                // Don't fail the appointment registration if email fails
            }
        }

        // Return appointment response
        return getAppointmentByNumber(appointmentNumber);
    }

    /**
     * Get appointment by appointment number
     */
    public AppointmentResponse getAppointmentByNumber(String appointmentNumber) {
        if (!ValidationUtil.isValidAppointmentNumber(appointmentNumber)) {
            throw new ValidationException("Invalid appointment number format");
        }

        Optional<Appointment> appointmentOptional = appointmentDao.findByAppointmentNumber(appointmentNumber);

        if (appointmentOptional.isEmpty()) {
            throw new NotFoundException("Appointment", appointmentNumber);
        }

        return mapToAppointmentResponse(appointmentOptional.get());
    }

    /**
     * Get appointments by date
     */
    public List<AppointmentResponse> getAppointmentsByDate(String dateStr) {
        LocalDate date = DateUtil.parseDate(dateStr);
        if (date == null) {
            throw new ValidationException("Invalid date format");
        }

        List<Appointment> appointments = appointmentDao.findByDate(date);
        return appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get appointments by patient
     */
    public List<AppointmentResponse> getAppointmentsByPatient(int patientId) {
        List<Appointment> appointments = appointmentDao.findByPatientId(patientId);
        return appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update appointment status
     */
    public boolean updateAppointmentStatus(String appointmentNumber, String status) {
        Optional<Appointment> appointmentOptional = appointmentDao.findByAppointmentNumber(appointmentNumber);

        if (appointmentOptional.isEmpty()) {
            throw new NotFoundException("Appointment", appointmentNumber);
        }

        // Validate status
        if (!isValidStatus(status)) {
            throw new ValidationException("Invalid appointment status");
        }

        return appointmentDao.updateStatus(appointmentOptional.get().getAppointmentId(), status);
    }

    /**
     * Cancel appointment
     */
    public boolean cancelAppointment(String appointmentNumber) {
        return updateAppointmentStatus(appointmentNumber, "CANCELLED");
    }

    /**
     * Mark appointment as completed
     */
    public boolean completeAppointment(String appointmentNumber) {
        return updateAppointmentStatus(appointmentNumber, "COMPLETED");
    }

    /**
     * Validate appointment request
     */
    private void validateAppointmentRequest(AppointmentRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request == null) {
            throw new ValidationException("Appointment request cannot be null");
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

        if (request.getDentistName() == null || request.getDentistName().trim().isEmpty()) {
            errors.put("dentistName", "Dentist name is required");
        }

        if (request.getTreatmentType() == null || request.getTreatmentType().trim().isEmpty()) {
            errors.put("treatmentType", "Treatment type is required");
        }

        if (request.getAppointmentDate() == null || !ValidationUtil.isValidDate(request.getAppointmentDate())) {
            errors.put("appointmentDate", "Valid appointment date is required");
        } else if (!ValidationUtil.isFutureDate(request.getAppointmentDate())) {
            errors.put("appointmentDate", "Appointment date must be today or in the future");
        }

        if (request.getAppointmentTime() == null || !ValidationUtil.isValidTime(request.getAppointmentTime())) {
            errors.put("appointmentTime", "Valid appointment time is required");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    /**
     * Check if status is valid
     */
    private boolean isValidStatus(String status) {
        return status != null && (
                status.equals("SCHEDULED") ||
                        status.equals("COMPLETED") ||
                        status.equals("CANCELLED") ||
                        status.equals("NO_SHOW")
        );
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

    /**
     * Search appointments by contact number
     */
    public List<AppointmentResponse> searchByContactNumber(String contactNumber) {
        if (!ValidationUtil.isValidPhoneNumber(contactNumber)) {
            throw new ValidationException("Invalid contact number format");
        }

        List<Appointment> appointments = appointmentDao.findByContactNumber(contactNumber);
        return appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search appointments by patient name
     */
    public List<AppointmentResponse> searchByPatientName(String patientName) {
        if (patientName == null || patientName.trim().length() < 2) {
            throw new ValidationException("Patient name must be at least 2 characters");
        }

        List<Appointment> appointments = appointmentDao.findByPatientName(patientName.trim());
        return appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all appointments
     */
    public List<AppointmentResponse> getAllAppointments() {
        List<Appointment> appointments = appointmentDao.findAllAppointments();
        return appointments.stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());
    }
}