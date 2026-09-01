package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.dao.PatientDao;
import com.sunrisedental.dto.request.PatientRequest;
import com.sunrisedental.dto.response.PatientResponse;
import com.sunrisedental.exception.NotFoundException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.model.Patient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test automation for PatientService
 * Tests patient registration, search, and validation
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientDao patientDao;

    @Mock
    private AppointmentDao appointmentDao;

    private PatientService patientService;

    private Patient testPatient;

    @BeforeEach
    void setUp() throws Exception {
        patientService = new PatientService();

        // Inject mocks
        Field patientDaoField = PatientService.class.getDeclaredField("patientDao");
        patientDaoField.setAccessible(true);
        patientDaoField.set(patientService, patientDao);

        Field appointmentDaoField = PatientService.class.getDeclaredField("appointmentDao");
        appointmentDaoField.setAccessible(true);
        appointmentDaoField.set(patientService, appointmentDao);

        // Create test patient
        testPatient = Patient.builder()
                .patientId(1)
                .patientCode("P001")
                .patientName("John Doe")
                .address("123 Main Street, Colombo")
                .contactNumber("+94-77-1234567")
                .email("john@example.com")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("TC-PAT-001: Register new patient successfully")
    void testRegisterPatientSuccess() {
        // Arrange
        PatientRequest request = new PatientRequest();
        request.setPatientName("John Doe");
        request.setContactNumber("+94-77-1234567");
        request.setAddress("123 Main Street");
        request.setEmail("john@example.com");

        when(patientDao.findByContactNumber("+94-77-1234567")).thenReturn(Optional.empty());
        when(patientDao.generateNextPatientCode()).thenReturn("P001");
        when(patientDao.createPatient(any(Patient.class))).thenReturn(1);
        when(patientDao.findByPatientCode("P001")).thenReturn(Optional.of(testPatient));

        // Act
        PatientResponse response = patientService.registerPatient(request);

        // Assert
        assertNotNull(response, "Patient response should not be null");
        assertEquals("P001", response.getPatientCode(), "Patient code should be P001");
        assertEquals("John Doe", response.getPatientName(), "Patient name should match");
        assertEquals("+94-77-1234567", response.getContactNumber(), "Contact should match");

        // Verify
        verify(patientDao).createPatient(any(Patient.class));
    }

    @Test
    @DisplayName("TC-PAT-002: Register patient with duplicate contact number")
    void testRegisterPatientDuplicateContact() {
        // Arrange
        PatientRequest request = new PatientRequest();
        request.setPatientName("Jane Doe");
        request.setContactNumber("+94-77-1234567");

        when(patientDao.findByContactNumber("+94-77-1234567")).thenReturn(Optional.of(testPatient));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            patientService.registerPatient(request);
        });

        assertTrue(exception.getMessage().contains("already exists"),
                "Error message should mention existing patient");

        // Verify createPatient was never called
        verify(patientDao, never()).createPatient(any(Patient.class));
    }

    @Test
    @DisplayName("TC-PAT-003: Register patient with invalid phone number")
    void testRegisterPatientInvalidPhone() {
        // Arrange
        PatientRequest request = new PatientRequest();
        request.setPatientName("John Doe");
        request.setContactNumber("123"); // Invalid phone

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            patientService.registerPatient(request);
        }, "Should throw ValidationException for invalid phone");
    }

    @Test
    @DisplayName("TC-PAT-004: Search patient by code successfully")
    void testGetPatientByCode() {
        // Arrange
        when(patientDao.findByPatientCode("P001")).thenReturn(Optional.of(testPatient));

        // Act
        PatientResponse response = patientService.getPatientByCode("P001");

        // Assert
        assertNotNull(response, "Patient response should not be null");
        assertEquals("P001", response.getPatientCode());
        assertEquals("John Doe", response.getPatientName());
    }

    @Test
    @DisplayName("TC-PAT-005: Search patient by non-existent code")
    void testGetPatientByNonExistentCode() {
        // Arrange
        when(patientDao.findByPatientCode("P999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            patientService.getPatientByCode("P999");
        }, "Should throw NotFoundException for non-existent patient");
    }

    @Test
    @DisplayName("TC-PAT-006: Search patients by name")
    void testSearchPatientsByName() {
        // Arrange
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientDao.findByName("John")).thenReturn(patients);

        // Act
        List<PatientResponse> responses = patientService.searchPatientsByName("John");

        // Assert
        assertNotNull(responses, "Response list should not be null");
        assertEquals(1, responses.size(), "Should return 1 patient");
        assertEquals("John Doe", responses.get(0).getPatientName());
    }

    @Test
    @DisplayName("TC-PAT-007: Search patients with too short name")
    void testSearchPatientsWithShortName() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            patientService.searchPatientsByName("J");
        }, "Should throw ValidationException for name shorter than 2 characters");
    }
}