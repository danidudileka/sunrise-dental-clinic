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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test automation for AppointmentService
 * Tests appointment booking, validation, and double booking prevention
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentDao appointmentDao;

    @Mock
    private PatientDao patientDao;

    @Mock
    private DentistDao dentistDao;

    @Mock
    private TreatmentDao treatmentDao;

    private AppointmentService appointmentService;

    private Patient testPatient;
    private Dentist testDentist;
    private Treatment testTreatment;

    @BeforeEach
    void setUp() throws Exception {
        appointmentService = new AppointmentService();

        // Inject mocks
        Field appointmentDaoField = AppointmentService.class.getDeclaredField("appointmentDao");
        appointmentDaoField.setAccessible(true);
        appointmentDaoField.set(appointmentService, appointmentDao);

        Field patientDaoField = AppointmentService.class.getDeclaredField("patientDao");
        patientDaoField.setAccessible(true);
        patientDaoField.set(appointmentService, patientDao);

        Field dentistDaoField = AppointmentService.class.getDeclaredField("dentistDao");
        dentistDaoField.setAccessible(true);
        dentistDaoField.set(appointmentService, dentistDao);

        Field treatmentDaoField = AppointmentService.class.getDeclaredField("treatmentDao");
        treatmentDaoField.setAccessible(true);
        treatmentDaoField.set(appointmentService, treatmentDao);

        // Create test data
        testPatient = Patient.builder()
                .patientId(1)
                .patientCode("P001")
                .patientName("John Doe")
                .contactNumber("+94-77-1234567")
                .email("john@example.com")
                .build();

        testDentist = Dentist.builder()
                .dentistId(1)
                .name("Dr. John Smith")
                .specialization("General Dentistry")
                .isActive(true)
                .build();

        testTreatment = Treatment.builder()
                .treatmentId(1)
                .treatmentName("General Checkup")
                .baseCost(new BigDecimal("2000.00"))
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("TC-APT-001: Register appointment successfully")
    void testRegisterAppointmentSuccess() {
        // Arrange
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientCode("P001");
        request.setDentistName("Dr. John Smith");
        request.setTreatmentType("General Checkup");
        request.setAppointmentDate("2026-03-20");
        request.setAppointmentTime("10:00");
        request.setConsultationFee(new BigDecimal("500.00"));

        Appointment appointment = Appointment.builder()
                .appointmentId(1)
                .appointmentNumber("APT202600001")
                .patientId(1)
                .dentistId(1)
                .treatmentId(1)
                .appointmentDate(LocalDate.parse("2026-03-20"))
                .appointmentTime(LocalTime.parse("10:00:00"))
                .consultationFee(new BigDecimal("500.00"))
                .status("SCHEDULED")
                .patientName("John Doe")
                .dentistName("Dr. John Smith")
                .treatmentName("General Checkup")
                .treatmentCost(new BigDecimal("2000.00"))
                .patientContactNumber("+94-77-1234567")
                .build();

        when(patientDao.findByPatientCode("P001")).thenReturn(Optional.of(testPatient));
        when(dentistDao.findByName("Dr. John Smith")).thenReturn(Optional.of(testDentist));
        when(treatmentDao.findByName("General Checkup")).thenReturn(Optional.of(testTreatment));
        when(appointmentDao.generateNextAppointmentNumber()).thenReturn("APT202600001");
        when(appointmentDao.isDoubleBooked(anyInt(), any(), any())).thenReturn(false);
        when(appointmentDao.createAppointment(any(Appointment.class))).thenReturn(1);
        when(appointmentDao.findByAppointmentNumber("APT202600001")).thenReturn(Optional.of(appointment));

        // Act
        AppointmentResponse response = appointmentService.registerAppointment(request, 1);

        // Assert
        assertNotNull(response, "Appointment response should not be null");
        assertEquals("APT202600001", response.getAppointmentNumber());
        assertEquals("John Doe", response.getPatientName());
        assertEquals("Dr. John Smith", response.getDentistName());
        assertEquals("SCHEDULED", response.getStatus());

        // Verify
        verify(appointmentDao).createAppointment(any(Appointment.class));
    }

    @Test
    @DisplayName("TC-APT-002: Register appointment with invalid patient code")
    void testRegisterAppointmentInvalidPatient() {
        // Arrange
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientCode("P999");
        request.setDentistName("Dr. John Smith");
        request.setTreatmentType("General Checkup");
        request.setAppointmentDate("2026-11-20");
        request.setAppointmentTime("10:00");

        // Only stub what's needed - patient not found
//        when(patientDao.findByPatientCode("P999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            appointmentService.registerAppointment(request, 1);
        }, "Should throw ValidationException for invalid patient code");

        // Verify createAppointment was never called
        verify(appointmentDao, never()).createAppointment(any(Appointment.class));
    }

    @Test
    @DisplayName("TC-APT-003: Prevent double booking")
    void testPreventDoubleBooking() {
        // Arrange
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientCode("P001");
        request.setDentistName("Dr. John Smith");
        request.setTreatmentType("General Checkup");
        request.setAppointmentDate("2026-11-20");
        request.setAppointmentTime("10:00");

        // Stub only what's needed for the flow up to double booking check
//        when(patientDao.findByPatientCode("P001")).thenReturn(Optional.of(testPatient));
//        when(dentistDao.findByName("Dr. John Smith")).thenReturn(Optional.of(testDentist));
//        when(treatmentDao.findByName("General Checkup")).thenReturn(Optional.of(testTreatment));
//        when(appointmentDao.isDoubleBooked(anyInt(), any(), any())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            appointmentService.registerAppointment(request, 1);
        }, "Should throw ValidationException for double booking");

        // Verify createAppointment was never called
        verify(appointmentDao, never()).createAppointment(any(Appointment.class));
    }

    @Test
    @DisplayName("TC-APT-004: Register appointment with empty patient code")
    void testRegisterAppointmentEmptyPatientCode() {
        // Arrange
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientCode("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            appointmentService.registerAppointment(request, 1);
        }, "Should throw ValidationException for empty patient code");
    }

    @Test
    @DisplayName("TC-APT-005: Get appointment by number")
    void testGetAppointmentByNumber() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .appointmentId(1)
                .appointmentNumber("APT202600001")
                .patientName("John Doe")
                .dentistName("Dr. John Smith")
                .treatmentName("General Checkup")
                .appointmentDate(LocalDate.parse("2026-03-20"))
                .appointmentTime(LocalTime.parse("10:00:00"))
                .consultationFee(new BigDecimal("500.00"))
                .status("SCHEDULED")
                .build();

        when(appointmentDao.findByAppointmentNumber("APT202600001")).thenReturn(Optional.of(appointment));

        // Act
        AppointmentResponse response = appointmentService.getAppointmentByNumber("APT202600001");

        // Assert
        assertNotNull(response);
        assertEquals("APT202600001", response.getAppointmentNumber());
        assertEquals("John Doe", response.getPatientName());
    }

    @Test
    @DisplayName("TC-APT-006: Get non-existent appointment")
    void testGetNonExistentAppointment() {
        // Arrange
        when(appointmentDao.findByAppointmentNumber("APT999999999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            appointmentService.getAppointmentByNumber("APT999999999");
        });
    }

    @Test
    @DisplayName("TC-APT-007: Complete appointment successfully")
    void testCompleteAppointment() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .appointmentId(1)
                .appointmentNumber("APT202600001")
                .status("SCHEDULED")
                .build();

        when(appointmentDao.findByAppointmentNumber("APT202600001")).thenReturn(Optional.of(appointment));
        when(appointmentDao.updateStatus(1, "COMPLETED")).thenReturn(true);

        // Act
        boolean result = appointmentService.completeAppointment("APT202600001");

        // Assert
        assertTrue(result, "Appointment should be completed successfully");
        verify(appointmentDao).updateStatus(1, "COMPLETED");
    }
}