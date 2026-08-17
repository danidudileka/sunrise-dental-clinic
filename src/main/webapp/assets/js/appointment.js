/**
 * Appointment management functionality
 */

let currentAppointmentNumber = null;

document.addEventListener('DOMContentLoaded', function() {
    // Set minimum date for appointment date field
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
    // Hide all tabs
    const tabs = document.querySelectorAll('.tab-content');
    tabs.forEach(tab => {
        tab.classList.remove('active');
    });

    // Show selected tab
    document.getElementById(`${tabName}-tab`).classList.add('active');

    // Update active button
    const buttons = document.querySelectorAll('.tab-btn');
    buttons.forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');

    // Load data if needed
    if (tabName === 'list') {
        loadTodayAppointments();
    }
}

/**
 * Register new appointment
 */
async function registerAppointment(e) {
    e.preventDefault();

    // Clear previous messages
    hideAlert('appointment-success');
    hideAlert('appointment-error');

    // Gather form data
    const appointmentData = {
        patientName: document.getElementById('patient-name').value,
        address: document.getElementById('address').value,
        contactNumber: document.getElementById('contact-number').value,
        email: document.getElementById('email').value,
        dentistName: document.getElementById('dentist-name').value,
        treatmentType: document.getElementById('treatment-type').value,
        appointmentDate: document.getElementById('appointment-date').value,
        appointmentTime: document.getElementById('appointment-time').value,
        notes: document.getElementById('notes').value
    };

    // Validate required fields
    if (!appointmentData.patientName || !appointmentData.contactNumber ||
        !appointmentData.dentistName || !appointmentData.treatmentType ||
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

            // Reload today's appointments
            loadTodayAppointments();
        }

    } catch (error) {
        showAlert('appointment-error', error.message, 'error');
    }
}

/**
 * Search appointment by number
 */
async function searchAppointment() {
    const appointmentNumber = document.getElementById('search-appointment-number').value;

    if (!appointmentNumber) {
        showAlert('search-error', 'Please enter an appointment number', 'error');
        return;
    }

    try {
        const response = await apiGet(`/appointments/number/${appointmentNumber}`);

        if (response.status === 'SUCCESS') {
            displayAppointmentDetails(response.data);
            currentAppointmentNumber = appointmentNumber;
        }

    } catch (error) {
        showAlert('search-error', error.message, 'error');
        document.getElementById('appointment-details').style.display = 'none';
    }
}

/**
 * Display appointment details
 */
function displayAppointmentDetails(appointment) {
    const detailsBody = document.getElementById('appointment-details-body');

    const rows = [
        ['Appointment Number', appointment.appointmentNumber],
        ['Patient Name', appointment.patientName],
        ['Contact Number', appointment.contactNumber],
        ['Dentist', appointment.dentistName],
        ['Treatment', appointment.treatmentType],
        ['Treatment Cost', formatCurrency(appointment.treatmentCost)],
        ['Consultation Fee', formatCurrency(appointment.consultationFee)],
        ['Date', formatDate(appointment.appointmentDate)],
        ['Time', formatTime(appointment.appointmentTime)],
        ['Status', appointment.status]
    ];

    if (appointment.notes) {
        rows.push(['Notes', appointment.notes]);
    }

    detailsBody.innerHTML = rows.map(([label, value]) => `
        <tr>
            <td><strong>${label}</strong></td>
            <td>${value || 'N/A'}</td>
        </tr>
    `).join('');

    document.getElementById('appointment-details').style.display = 'block';
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
            <tr>
                <td colspan="8" class="text-center">Error loading appointments</td>
            </tr>
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
            <tr>
                <td colspan="8" class="text-center">No appointments for today</td>
            </tr>
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
                <button onclick="viewAppointment('${appointment.appointmentNumber}')" 
                        class="btn btn-sm btn-secondary">View</button>
            </td>
        </tr>
    `).join('');
}

/**
 * View appointment details
 */
function viewAppointment(appointmentNumber) {
    document.getElementById('search-appointment-number').value = appointmentNumber;
    showTab('search');
    searchAppointment();
}

/**
 * Complete appointment
 */
async function completeAppointment() {
    if (!currentAppointmentNumber) {
        showAlert('search-error', 'No appointment selected', 'error');
        return;
    }

    if (!confirm('Mark this appointment as completed?')) {
        return;
    }

    try {
        const response = await apiPost('/appointments/complete', {
            appointmentNumber: currentAppointmentNumber
        });

        if (response.status === 'SUCCESS') {
            showAlert('search-error', 'Appointment completed successfully', 'success');
            searchAppointment();
        }

    } catch (error) {
        showAlert('search-error', error.message, 'error');
    }
}

/**
 * Cancel appointment
 */
async function cancelAppointment() {
    if (!currentAppointmentNumber) {
        showAlert('search-error', 'No appointment selected', 'error');
        return;
    }

    if (!confirm('Cancel this appointment?')) {
        return;
    }

    try {
        const response = await apiPost('/appointments/cancel', {
            appointmentNumber: currentAppointmentNumber
        });

        if (response.status === 'SUCCESS') {
            showAlert('search-error', 'Appointment cancelled successfully', 'success');
            searchAppointment();
        }

    } catch (error) {
        showAlert('search-error', error.message, 'error');
    }
}

/**
 * View bill for appointment
 */
function viewBill() {
    if (currentAppointmentNumber) {
        window.location.href = `sunrise-dental-clinic/billing.html?appointment=${currentAppointmentNumber}`;
    }
}