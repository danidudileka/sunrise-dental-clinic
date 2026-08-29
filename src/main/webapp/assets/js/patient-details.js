/**
 * Patient details page functionality
 */

document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const patientCode = urlParams.get('code');

    if (patientCode) {
        loadPatientDetails(patientCode);
    } else {
        showAlert('patient-error', 'No patient ID provided', 'error');
    }
});

/**
 * Load patient details with appointments
 */
async function loadPatientDetails(patientCode) {
    try {
        const response = await apiGet(`/patients/${patientCode}/appointments`);

        if (response.status === 'SUCCESS') {
            displayPatientDetails(response.data);
        }
    } catch (error) {
        document.getElementById('patient-details-container').innerHTML = `
            <div class="alert alert-error">
                <i class="fas fa-exclamation-circle"></i> ${error.message}
            </div>
        `;
    }
}

/**
 * Display patient details
 */
function displayPatientDetails(data) {
    const container = document.getElementById('patient-details-container');
    const patient = data.patient;
    const appointments = data.appointments;

    container.innerHTML = `
        <div class="card">
            <div class="card-header">
                <h2>Patient Information</h2>
            </div>
            
            <div class="table-responsive">
                <table class="table">
                    <tbody>
                        <tr><td><strong>Patient ID</strong></td><td>${patient.patientCode}</td></tr>
                        <tr><td><strong>Name</strong></td><td>${patient.patientName}</td></tr>
                        <tr><td><strong>Contact Number</strong></td><td>${patient.contactNumber}</td></tr>
                        <tr><td><strong>Email</strong></td><td>${patient.email || 'N/A'}</td></tr>
                        <tr><td><strong>Address</strong></td><td>${patient.address || 'N/A'}</td></tr>
                        <tr><td><strong>Date of Birth</strong></td><td>${formatDate(patient.dateOfBirth) || 'N/A'}</td></tr>
                        <tr><td><strong>Gender</strong></td><td>${patient.gender || 'N/A'}</td></tr>
                        <tr><td><strong>Blood Group</strong></td><td>${patient.bloodGroup || 'N/A'}</td></tr>
                        ${patient.medicalHistory ? `<tr><td><strong>Medical History</strong></td><td>${patient.medicalHistory}</td></tr>` : ''}
                        <tr><td><strong>Registered Date</strong></td><td>${formatDate(patient.createdAt)}</td></tr>
                    </tbody>
                </table>
            </div>
        </div>
        
        <div class="card">
            <div class="card-header">
                <h2>Appointment History</h2>
                <p>All appointments (past and upcoming)</p>
            </div>
            
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Appointment #</th>
                            <th>Dentist</th>
                            <th>Treatment</th>
                            <th>Date</th>
                            <th>Time</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${displayAppointmentsList(appointments)}
                    </tbody>
                </table>
            </div>
        </div>
    `;
}

/**
 * Generate appointments list HTML
 */
function displayAppointmentsList(appointments) {
    if (!appointments || appointments.length === 0) {
        return `
            <tr>
                <td colspan="7" class="text-center">No appointments found</td>
            </tr>
        `;
    }

    return appointments.map(appointment => `
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
            <td>
                <a href="appointment-details.html?number=${appointment.appointmentNumber}" 
                   class="btn btn-sm btn-secondary">
                    <i class="fas fa-eye"></i> View
                </a>
            </td>
        </tr>
    `).join('');
}