/**
 * Appointment management functionality
 */

let currentPatientCode = null;

document.addEventListener('DOMContentLoaded', function() {
    // Set minimum date
    const dateInput = document.getElementById('appointment-date');
    if (dateInput) {
        dateInput.min = new Date().toISOString().split('T')[0];
    }

    // Load dentists and treatments
    loadDentists();
    loadTreatments();

    // Load today's appointments
    loadTodayAppointments();

    // Form handler
    const appointmentForm = document.getElementById('appointment-form');
    if (appointmentForm) {
        appointmentForm.addEventListener('submit', registerAppointment);
    }

    // Tab handling
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const tabName = this.dataset.tab;
            showTab(tabName);
        });
    });
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
        if (btn.dataset.tab === tabName) {
            btn.classList.add('active');
        }
    });

    if (tabName === 'today') {
        loadTodayAppointments();
    }
}

/**
 * Load dentists from database
 */
async function loadDentists() {
    try {
        const response = await apiGet('/data/dentists');

        if (response.status === 'SUCCESS') {
            const dentistSelect = document.getElementById('dentist-name');
            const dentists = response.data;

            dentistSelect.innerHTML = '<option value="">Select Dentist</option>';

            dentists.forEach(dentist => {
                const option = document.createElement('option');
                option.value = dentist.name;
                option.textContent = `${dentist.name} - ${dentist.specialization || 'General'}`;
                dentistSelect.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Error loading dentists:', error);
    }
}

/**
 * Load treatments from database
 */
async function loadTreatments() {
    try {
        const response = await apiGet('/data/treatments');

        if (response.status === 'SUCCESS') {
            const treatmentSelect = document.getElementById('treatment-type');
            const treatments = response.data;

            treatmentSelect.innerHTML = '<option value="">Select Treatment</option>';

            treatments.forEach(treatment => {
                const option = document.createElement('option');
                option.value = treatment.treatmentName;
                option.textContent = `${treatment.treatmentName} - Rs. ${treatment.baseCost}`;
                treatmentSelect.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Error loading treatments:', error);
    }
}

/**
 * Search patient for appointment
 */
async function searchPatientForAppointment() {
    const patientCode = document.getElementById('patient-code').value.trim();

    if (!patientCode) {
        showAlert('appointment-error', 'Please enter a patient ID', 'error');
        return;
    }

    try {
        const response = await apiGet(`/patients/code/${patientCode}`);

        if (response.status === 'SUCCESS') {
            currentPatientCode = patientCode;
            displayPatientInfo(response.data);
            document.getElementById('appointment-form').style.display = 'block';
            hideAlert('appointment-error');
        }
    } catch (error) {
        showAlert('appointment-error', 'Invalid patient ID. Patient not found.', 'error');
        document.getElementById('patient-info').style.display = 'none';
        document.getElementById('appointment-form').style.display = 'none';
    }
}

/**
 * Display patient info
 */
function displayPatientInfo(patient) {
    const infoDiv = document.getElementById('patient-info');

    infoDiv.innerHTML = `
        <div class="card" style="background: #e7f1ff; border: 1px solid #bae6fd;">
            <h4><i class="fas fa-user"></i> Patient Information</h4>
            <div class="table-responsive">
                <table class="table">
                    <tbody>
                        <tr><td><strong>Patient ID</strong></td><td>${patient.patientCode}</td></tr>
                        <tr><td><strong>Name</strong></td><td>${patient.patientName}</td></tr>
                        <tr><td><strong>Contact</strong></td><td>${patient.contactNumber}</td></tr>
                        ${patient.email ? `<tr><td><strong>Email</strong></td><td>${patient.email}</td></tr>` : ''}
                    </tbody>
                </table>
            </div>
        </div>
    `;

    infoDiv.style.display = 'block';
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
            alert(`Appointment registered successfully!\n\nAppointment Number: ${response.data.appointmentNumber}`);

            // Reset form
            document.getElementById('appointment-form').reset();
            document.getElementById('patient-code').value = '';
            document.getElementById('patient-info').style.display = 'none';
            document.getElementById('appointment-form').style.display = 'none';
            currentPatientCode = null;

            loadTodayAppointments();
        }
    } catch (error) {
        showAlert('appointment-error', error.message, 'error');
    }
}

/**
 * Search appointments
 */
async function searchAppointments() {
    const searchType = document.getElementById('search-type').value;
    const searchTerm = document.getElementById('search-term').value.trim();

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
                break;

            case 'contact':
                if (!searchTerm) {
                    showAlert('search-error', 'Please enter a contact number', 'error');
                    return;
                }
                response = await apiGet(`/appointments/contact/${encodeURIComponent(searchTerm)}`);
                break;

            case 'name':
                if (!searchTerm || searchTerm.length < 2) {
                    showAlert('search-error', 'Please enter at least 2 characters', 'error');
                    return;
                }
                response = await apiGet(`/appointments/name/${encodeURIComponent(searchTerm)}`);
                break;

            case 'date':
                if (!searchTerm) {
                    showAlert('search-error', 'Please select a date', 'error');
                    return;
                }
                response = await apiGet(`/appointments/date/${searchTerm}`);
                break;

            case 'all':
                response = await apiGet('/appointments/all');
                break;
        }

        if (response.status === 'SUCCESS') {
            if (searchType === 'appointment') {
                // Single result
                displayAppointmentsList([response.data]);
            } else {
                displayAppointmentsList(response.data);
            }
        }
    } catch (error) {
        showAlert('search-error', error.message, 'error');
    }
}

/**
 * Display appointments list
 */
function displayAppointmentsList(appointments) {
    const listContainer = document.getElementById('appointments-list');
    const tableBody = document.getElementById('appointments-list-body');

    if (!appointments || appointments.length === 0) {
        tableBody.innerHTML = `
            <tr><td colspan="8" class="text-center">No appointments found</td></tr>
        `;
        listContainer.style.display = 'block';
        return;
    }

    tableBody.innerHTML = appointments.map(appointment => `
        <tr>
            <td>${appointment.appointmentNumber}</td>
            <td>${appointment.patientName}</td>
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
                <a href="appointment-details.html?number=${appointment.appointmentNumber}" 
                   class="btn btn-sm btn-secondary">
                    <i class="fas fa-eye"></i> View
                </a>
            </td>
        </tr>
    `).join('');

    listContainer.style.display = 'block';
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
        document.getElementById('today-appointments-body').innerHTML = `
            <tr><td colspan="7" class="text-center">Error loading appointments</td></tr>
        `;
    }
}

/**
 * Display today's appointments
 */
function displayTodayAppointments(appointments) {
    const tableBody = document.getElementById('today-appointments-body');

    if (!appointments || appointments.length === 0) {
        tableBody.innerHTML = `
            <tr><td colspan="7" class="text-center">No appointments for today</td></tr>
        `;
        return;
    }

    tableBody.innerHTML = appointments.map(appointment => `
        <tr>
            <td>${appointment.appointmentNumber}</td>
            <td>${appointment.patientName}</td>
            <td>${appointment.dentistName}</td>
            <td>${appointment.treatmentType}</td>
            <td>${formatTime(appointment.appointmentTime)}</td>
            <td>
                <span class="status-badge status-${appointment.status.toLowerCase()}">
                    ${appointment.status}
                </span>
            </td>
            <td>
                <a href="appointment-details.html?number=${appointment.appointmentNumber}" 
                   class="btn btn-sm btn-secondary">
                    <i class="fas fa-eye"></i> View
                </a>
            </td>
        </tr>
    `).join('');
}