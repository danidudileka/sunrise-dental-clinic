/**
 * Patient management functionality
 */

let currentPatientCode = null;

document.addEventListener('DOMContentLoaded', function() {
    loadAllPatients();

    // Patient form handler
    const patientForm = document.getElementById('patient-form');
    if (patientForm) {
        patientForm.addEventListener('submit', registerPatient);
    }
});

/**
 * Show tab content
 */
function showTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });

    document.getElementById(`${tabName}-tab`).classList.add('active');

    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');

    if (tabName === 'all') {
        loadAllPatients();
    }
}

/**
 * Register new patient
 */
async function registerPatient(e) {
    e.preventDefault();

    hideAlert('patient-success');
    hideAlert('patient-error');

    const patientData = {
        patientName: document.getElementById('patient-name').value,
        contactNumber: document.getElementById('contact-number').value,
        email: document.getElementById('email').value,
        dateOfBirth: document.getElementById('date-of-birth').value,
        gender: document.getElementById('gender').value,
        bloodGroup: document.getElementById('blood-group').value,
        address: document.getElementById('address').value,
        medicalHistory: document.getElementById('medical-history').value
    };

    if (!patientData.patientName || !patientData.contactNumber) {
        showAlert('patient-error', 'Please fill in required fields', 'error');
        return;
    }

    try {
        const response = await apiPost('/patients/register', patientData);

        if (response.status === 'SUCCESS') {
            // Show patient ID in modal
            document.getElementById('modal-patient-id').textContent = response.data.patientCode;
            document.getElementById('patient-id-modal').style.display = 'block';

            // Reset form
            document.getElementById('patient-form').reset();

            // Reload all patients
            loadAllPatients();
        }

    } catch (error) {
        showAlert('patient-error', error.message, 'error');
    }
}

/**
 * Search patient
 */
async function searchPatient() {
    const searchType = document.getElementById('search-type').value;
    const searchTerm = document.getElementById('search-term').value;

    if (!searchTerm) {
        showAlert('search-error', 'Please enter a search term', 'error');
        return;
    }

    try {
        let response;

        if (searchType === 'code') {
            response = await apiGet(`/patients/code/${searchTerm}`);
        } else if (searchType === 'contact') {
            response = await apiGet(`/patients/contact/${encodeURIComponent(searchTerm)}`);
        } else {
            response = await apiGet(`/patients/search/${encodeURIComponent(searchTerm)}`);
        }

        if (response.status === 'SUCCESS') {
            if (searchType === 'name') {
                // Show list of patients
                displayPatientsList(response.data);
            } else {
                // Show single patient with appointments
                displayPatientWithAppointments(response.data);
            }
        }

    } catch (error) {
        showAlert('search-error', error.message, 'error');
    }
}

/**
 * Display patient with appointments
 */
async function displayPatientWithAppointments(patient) {
    currentPatientCode = patient.patientCode;

    document.getElementById('patient-details').style.display = 'block';

    const detailsBody = document.getElementById('patient-details-body');
    detailsBody.innerHTML = `
        <tr><td><strong>Patient ID</strong></td><td>${patient.patientCode}</td></tr>
        <tr><td><strong>Name</strong></td><td>${patient.patientName}</td></tr>
        <tr><td><strong>Contact</strong></td><td>${patient.contactNumber}</td></tr>
        <tr><td><strong>Email</strong></td><td>${patient.email || 'N/A'}</td></tr>
        <tr><td><strong>Address</strong></td><td>${patient.address || 'N/A'}</td></tr>
        <tr><td><strong>Gender</strong></td><td>${patient.gender || 'N/A'}</td></tr>
        <tr><td><strong>Blood Group</strong></td><td>${patient.bloodGroup || 'N/A'}</td></tr>
    `;

    // Load appointments
    try {
        const response = await apiGet(`/patients/${patient.patientCode}/appointments`);
        if (response.status === 'SUCCESS') {
            displayPatientAppointments(response.data.appointments);
        }
    } catch (error) {
        console.error('Error loading appointments:', error);
    }
}

/**
 * Display patient appointments
 */
function displayPatientAppointments(appointments) {
    const tableBody = document.getElementById('patient-appointments-body');

    if (!appointments || appointments.length === 0) {
        tableBody.innerHTML = `
            <tr><td colspan="6" class="text-center">No appointments found</td></tr>
        `;
        return;
    }

    tableBody.innerHTML = appointments.map(appointment => `
        <tr>
            <td>${appointment.appointmentNumber}</td>
            <td>${appointment.dentistName}</td>
            <td>${appointment.treatmentType}</td>
            <td>${formatDate(appointment.appointmentDate)}</td>
            <td>${formatTime(appointment.appointmentTime)}</td>
            <td>
                <span class="status-badge status-${appointment.status.toLowerCase()}">
                    ${appointment.status}
                </span>
            </td>
        </tr>
    `).join('');
}

/**
 * Display patients list
 */
function displayPatientsList(patients) {
    document.getElementById('patient-details').style.display = 'none';

    if (!patients || patients.length === 0) {
        showAlert('search-error', 'No patients found', 'error');
        return;
    }

    const tableBody = document.getElementById('all-patients-body');
    tableBody.innerHTML = patients.map(patient => `
        <tr>
            <td>${patient.patientCode}</td>
            <td>${patient.patientName}</td>
            <td>${patient.contactNumber}</td>
            <td>${patient.email || 'N/A'}</td>
            <td>${formatDate(patient.createdAt)}</td>
            <td>
                <button onclick="viewPatient('${patient.patientCode}')" class="btn btn-sm btn-secondary">
                    <i class="fas fa-eye"></i> View
                </button>
            </td>
        </tr>
    `).join('');
}

/**
 * Load all patients
 */
async function loadAllPatients() {
    try {
        const response = await apiGet('/patients/all');

        if (response.status === 'SUCCESS') {
            const tableBody = document.getElementById('all-patients-body');
            const patients = response.data;

            if (!patients || patients.length === 0) {
                tableBody.innerHTML = `
                    <tr><td colspan="6" class="text-center">No patients registered</td></tr>
                `;
                return;
            }

            tableBody.innerHTML = patients.map(patient => `
                <tr>
                    <td>${patient.patientCode}</td>
                    <td>${patient.patientName}</td>
                    <td>${patient.contactNumber}</td>
                    <td>${patient.email || 'N/A'}</td>
                    <td>${formatDate(patient.createdAt)}</td>
                    <td>
                        <button onclick="viewPatient('${patient.patientCode}')" class="btn btn-sm btn-secondary">
                            <i class="fas fa-eye"></i> View
                        </button>
                    </td>
                </tr>
            `).join('');
        }

    } catch (error) {
        console.error('Error loading patients:', error);
    }
}

/**
 * View patient details
 */
async function viewPatient(patientCode) {
    try {
        const response = await apiGet(`/patients/code/${patientCode}`);

        if (response.status === 'SUCCESS') {
            displayPatientWithAppointments(response.data);
            showTab('search');
        }

    } catch (error) {
        showAlert('search-error', error.message, 'error');
    }
}

/**
 * Close modal
 */
function closeModal() {
    document.getElementById('patient-id-modal').style.display = 'none';
}