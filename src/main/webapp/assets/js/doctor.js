/**
 * Doctor dashboard functionality
 */

document.addEventListener('DOMContentLoaded', function() {
    loadDoctorDashboard();
});

/**
 * Load doctor dashboard data
 */
async function loadDoctorDashboard() {
    try {
        // Load revenue
        const revenueResponse = await apiGet('/doctor/revenue');
        if (revenueResponse.status === 'SUCCESS') {
            displayRevenue(revenueResponse.data);
        }

        // Load appointments
        const appointmentsResponse = await apiGet('/doctor/appointments');
        if (appointmentsResponse.status === 'SUCCESS') {
            displayAppointments(appointmentsResponse.data);
        }

        // Load patients
        const patientsResponse = await apiGet('/doctor/patients');
        if (patientsResponse.status === 'SUCCESS') {
            displayPatients(patientsResponse.data);
        }

    } catch (error) {
        console.error('Error loading doctor dashboard:', error);
    }
}

/**
 * Display revenue stats
 */
function displayRevenue(data) {
    document.getElementById('completed-revenue').textContent = formatCurrency(data.completedRevenue);
    document.getElementById('upcoming-revenue').textContent = formatCurrency(data.upcomingRevenue);
    document.getElementById('total-revenue').textContent = formatCurrency(data.totalRevenue);
}

/**
 * Display appointments
 */
function displayAppointments(appointments) {
    const tableBody = document.getElementById('doctor-appointments-body');

    if (!appointments || appointments.length === 0) {
        tableBody.innerHTML = `
            <tr><td colspan="7" class="text-center">No appointments found</td></tr>
        `;
        return;
    }

    tableBody.innerHTML = appointments.map(appointment => `
        <tr>
            <td>${appointment.appointmentNumber}</td>
            <td>${appointment.patientName}</td>
            <td>${appointment.patientContactNumber || 'N/A'}</td>
            <td>${appointment.treatmentName}</td>
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
 * Display patients
 */
function displayPatients(patients) {
    const tableBody = document.getElementById('doctor-patients-body');

    if (!patients || patients.length === 0) {
        tableBody.innerHTML = `
            <tr><td colspan="4" class="text-center">No patients found</td></tr>
        `;
        return;
    }

    tableBody.innerHTML = patients.map(patient => `
        <tr>
            <td>${patient.patientCode}</td>
            <td>${patient.patientName}</td>
            <td>${patient.contactNumber}</td>
            <td>${patient.email || 'N/A'}</td>
        </tr>
    `).join('');
}