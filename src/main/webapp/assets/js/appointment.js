/**
 * Appointment management functionality
 */

let currentPatientCode = null;
let currentAppointmentNumber = null;

document.addEventListener('DOMContentLoaded', function() {
    // Set minimum date
    const dateInput = document.getElementById('appointment-date');
    if (dateInput) {
        const today = new Date().toISOString().split('T')[0];
        dateInput.min = today;
    }

    // Load today's appointments
    loadTodayAppointments();

    // Appointment form handler
    const appointmentForm = document.getElementById('appointment-form');
    if (appointmentForm) {
        appointmentForm.addEventListener('submit', registerAppointment);
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

    if (tabName === 'today') {
        loadTodayAppointments();
    }
}

/**
 * Search patient for appointment
 */
async function searchPatientForAppointment() {
    const patientCode = document.getElementById('patient-code').value;

    if (!patientCode) {
        showAlert('appointment-error', 'Please enter a patient ID', 'error');
        return;
    }

    try {
        const response = await apiGet(`/patients/code/${patientCode}`);

        if (response.status === 'SUCCESS') {
            currentPatientCode = patientCode;
            displayPatientInfo(response.data);
            document.getElementById('appointment-form-section').style.display = 'block';
            hideAlert('appointment-error');
        }

    } catch (error) {
        showAlert('appointment-error', 'Invalid patient ID. Patient not found.', 'error');
        document.getElementById('patient-info').style.display = 'none';
        document.getElementById('appointment-form-section').style.display = 'none';
    }
}

/**
 * Display patient info
 */
function displayPatientInfo(patient) {
    const infoBody = document.getElementById('patient-info-body');

    infoBody.innerHTML = `
        <tr><td><strong>Patient ID</strong></td><td>${patient.patientCode}</td></tr>
        <tr><td><strong>Name</strong></td><td>${patient.patientName}</td></tr>
        <tr><td><strong>Contact</strong></td><td>${patient.contactNumber}</td></tr>
        <tr><td><strong>Email</strong></td><td>${patient.email || 'N/A'}</td></tr>
    `;

    document.getElementById('patient-info').style.display = 'block';
}

/**
 * Register appointment
 */
async function registerAppointment(e) {
    e.preventDefault();

    if (!currentPatientCode) {
        showAlert('appointment-error', 'Please search for a patient first', 'error');
        return;
    }

    hideAlert('appointment-success');
    hideAlert('appointment-error');

    const appointmentData = {
        patientCode: currentPatientCode,
        dentistName: document.getElementById('dentist-name').value,
        treatmentType: document.getElementById('treatment-type').value,
        appointmentDate: document.getElementById('appointment-date').value,
        appointmentTime: document.getElementById('appointment-time').value,
        notes: document.getElementById('notes').value
    };

    if (!appointmentData.dentistName || !appointmentData.treatmentType ||
        !appointmentData.appointmentDate || !appointmentData.appointmentTime) {
        showAlert('appointment-error', 'Please fill in all required fields', 'error');
        return;
    }

    try {
        const response = await apiPost('/appointments/register', appointmentData);

        if (response.status === 'SUCCESS') {
            showAlert('appointment-success',
                `Appointment registered successfully! Appointment Number: ${response.data.appointmentNumber}`,
                'success');

            // Reset form
            document.getElementById('appointment-form').reset();
            document.getElementById('patient-code').value = '';
            document.getElementById('patient-info').style.display = 'none';
            document.getElementById('appointment-form-section').style.display = 'none';
            currentPatientCode = null;

            // Reload today's appointments
            loadTodayAppointments();
        }

    } catch (error) {
        showAlert('appointment-error', error.message, 'error');
    }
}

/**
 * Change appointment search type
 */
function changeAppointmentSearchType() {
    const searchType = document.getElementById('appointment-search-type').value;
    const searchTerm = document.getElementById('appointment-search-term');

    switch (searchType) {
        case 'appointment':
            searchTerm.placeholder = 'Enter appointment number (e.g., APT202400001)';
            break;
        case 'contact':
            searchTerm.placeholder = 'Enter contact number (e.g., +94-77-1234567)';
            break;
        case 'name':
            searchTerm.placeholder = 'Enter patient name (e.g., John)';
            break;
        case 'date':
            searchTerm.placeholder = 'Enter date (e.g., 2024-03-15)';
            searchTerm.type = 'date';
            break;
        case 'all':
            searchTerm.placeholder = 'Click search to load all appointments';
            searchTerm.type = 'text';
            searchTerm.disabled = true;
            break;
        default:
            searchTerm.type = 'text';
            searchTerm.disabled = false;
    }

    if (searchType !== 'all' && searchType !== 'date') {
        searchTerm.type = 'text';
        searchTerm.disabled = false;
    }
}

/**
 * Search appointments
 */
async function searchAppointments() {
    const searchType = document.getElementById('appointment-search-type').value;
    const searchTerm = document.getElementById('appointment-search-term').value;

    hideAlert('search-error');

    try {
        let response;

        switch (searchType) {
            case 'appointment':
                if (!searchTerm) {
                    showAlert('search-error', 'Please enter an appointment number', 'error');
                    return;
                }
                response = await apiGet(`/appointments/number/${searchTerm}`);
                if (response.status === 'SUCCESS') {
                    displaySingleAppointment(response.data);
                }
                break;

            case 'contact':
                if (!searchTerm) {
                    showAlert('search-error', 'Please enter a contact number', 'error');
                    return;
                }
                response = await apiGet(`/appointments/contact/${encodeURIComponent(searchTerm)}`);
                if (response.status === 'SUCCESS') {
                    displayAppointmentsList(response.data);
                }
                break;

            case 'name':
                if (!searchTerm || searchTerm.length < 2) {
                    showAlert('search-error', 'Please enter at least 2 characters', 'error');
                    return;
                }
                response = await apiGet(`/appointments/name/${encodeURIComponent(searchTerm)}`);
                if (response.status === 'SUCCESS') {
                    displayAppointmentsList(response.data);
                }
                break;

            case 'date':
                if (!searchTerm) {
                    showAlert('search-error', 'Please select a date', 'error');
                    return;
                }
                response = await apiGet(`/appointments/date/${searchTerm}`);
                if (response.status === 'SUCCESS') {
                    displayAppointmentsList(response.data);
                }
                break;

            case 'all':
                response = await apiGet('/appointments/all');
                if (response.status === 'SUCCESS') {
                    displayAppointmentsList(response.data);
                }
                break;
        }

    } catch (error) {
        showAlert('search-error', error.message, 'error');
    }
}

/**
 * Display single appointment details
 */
function displaySingleAppointment(appointment) {
    const listContainer = document.getElementById('appointments-list');
    const tableBody = document.getElementById('appointments-list-body');

    tableBody.innerHTML = `
        <tr>
            <td>${appointment.appointmentNumber}</td>
            <td>${appointment.patientName}</td>
            <td>${appointment.contactNumber}</td>
            <td>${appointment.dentistName}</td>
            <td>${appointment.treatmentType}</td>
            <td>${formatDate(appointment.appointmentDate)}</td>
            <td>${formatTime(appointment.appointmentTime)}</td>
            <td>
                <span class="status-badge status-${appointment.status.toLowerCase()}">
                    ${appointment.status}
                </span>
            </td>
            <td>
                <button onclick="viewAppointmentActions('${appointment.appointmentNumber}')" 
                        class="btn btn-sm btn-secondary">
                    <i class="fas fa-eye"></i> View
                </button>
            </td>
        </tr>
    `;

    listContainer.style.display = 'block';
}

/**
 * Display appointments list
 */
function displayAppointmentsList(appointments) {
    const listContainer = document.getElementById('appointments-list');
    const tableBody = document.getElementById('appointments-list-body');

    if (!appointments || appointments.length === 0) {
        tableBody.innerHTML = `
            <tr><td colspan="9" class="text-center">No appointments found</td></tr>
        `;
        listContainer.style.display = 'block';
        return;
    }

    tableBody.innerHTML = appointments.map(appointment => `
        <tr>
            <td>${appointment.appointmentNumber}</td>
            <td>${appointment.patientName}</td>
            <td>${appointment.contactNumber}</td>
            <td>${appointment.dentistName}</td>
            <td>${appointment.treatmentType}</td>
            <td>${formatDate(appointment.appointmentDate)}</td>
            <td>${formatTime(appointment.appointmentTime)}</td>
            <td>
                <span class="status-badge status-${appointment.status.toLowerCase()}">
                    ${appointment.status}
                </span>
            </td>
            <td>
                <button onclick="viewAppointmentActions('${appointment.appointmentNumber}')" 
                        class="btn btn-sm btn-secondary">
                    <i class="fas fa-eye"></i> View
                </button>
            </td>
        </tr>
    `).join('');

    listContainer.style.display = 'block';
}

/**
 * View appointment actions
 */
function viewAppointmentActions(appointmentNumber) {
    currentAppointmentNumber = appointmentNumber;

    if (confirm('What would you like to do?\n\nOK - Complete appointment\nCancel - Cancel appointment')) {
        completeAppointment(appointmentNumber);
    } else if (confirm('Cancel this appointment?')) {
        cancelAppointment(appointmentNumber);
    }
}

/**
 * Complete appointment
 */
async function completeAppointment(appointmentNumber) {
    try {
        const response = await apiPost('/appointments/complete', {
            appointmentNumber: appointmentNumber
        });

        if (response.status === 'SUCCESS') {
            showAlert('search-error', 'Appointment completed successfully', 'success');
            searchAppointments();
        }

    } catch (error) {
        showAlert('search-error', error.message, 'error');
    }
}

/**
 * Cancel appointment
 */
async function cancelAppointment(appointmentNumber) {
    try {
        const response = await apiPost('/appointments/cancel', {
            appointmentNumber: appointmentNumber
        });

        if (response.status === 'SUCCESS') {
            showAlert('search-error', 'Appointment cancelled successfully', 'success');
            searchAppointments();
        }

    } catch (error) {
        showAlert('search-error', error.message, 'error');
    }
}

/**
 * Load today's appointments
 */
async function loadTodayAppointments() {
    const today = new Date().toISOString().split('T')[0];

    try {
        const response = await apiGet(`/appointments/date/${today}`);

        if (response.status === 'SUCCESS') {
            displayTodayAppointments(response.data);
        }

    } catch (error) {
        console.error('Error loading today\'s appointments:', error);
    }
}

/**
 * Display today's appointments
 */
function displayTodayAppointments(appointments) {
    const tableBody = document.getElementById('today-appointments-body');

    if (!appointments || appointments.length === 0) {
        tableBody.innerHTML = `
            <tr><td colspan="8" class="text-center">No appointments for today</td></tr>
        `;
        return;
    }

    tableBody.innerHTML = appointments.map(appointment => `
        <tr>
            <td>${appointment.appointmentNumber}</td>
            <td>${appointment.patientName}</td>
            <td>${appointment.contactNumber}</td>
            <td>${appointment.dentistName}</td>
            <td>${appointment.treatmentType}</td>
            <td>${formatTime(appointment.appointmentTime)}</td>
            <td>
                <span class="status-badge status-${appointment.status.toLowerCase()}">
                    ${appointment.status}
                </span>
            </td>
            <td>
                <button onclick="viewAppointmentActions('${appointment.appointmentNumber}')" 
                        class="btn btn-sm btn-secondary">
                    <i class="fas fa-eye"></i> View
                </button>
            </td>
        </tr>
    `).join('');
}