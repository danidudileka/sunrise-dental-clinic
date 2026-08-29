/**
 * Appointment details page functionality
 */

let currentAppointment = null;

document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const appointmentNumber = urlParams.get('number');

    if (appointmentNumber) {
        loadAppointmentDetails(appointmentNumber);
    } else {
        showAlert('details-error', 'No appointment number provided', 'error');
    }
});

/**
 * Load appointment details
 */
async function loadAppointmentDetails(appointmentNumber) {
    try {
        const response = await apiGet(`/appointments/number/${appointmentNumber}`);

        if (response.status === 'SUCCESS') {
            currentAppointment = response.data;
            displayAppointmentDetails(currentAppointment);
            loadPaymentDetails(appointmentNumber);
        }
    } catch (error) {
        showAlert('details-error', error.message, 'error');
    }
}

/**
 * Load payment details
 */
async function loadPaymentDetails(appointmentNumber) {
    try {
        const response = await apiGet(`/billing/appointment/${appointmentNumber}`);

        if (response.status === 'SUCCESS') {
            displayPaymentDetails(response.data);
        }
    } catch (error) {
        // No bill yet
        displayNoBill();
    }
}

/**
 * Display appointment details
 */
function displayAppointmentDetails(appointment) {
    const container = document.getElementById('appointment-details-container');

    container.innerHTML = `
        <div class="card">
            <div class="card-header">
                <h2>Appointment Information</h2>
            </div>
            
            <div class="table-responsive">
                <table class="table">
                    <tbody>
                        <tr><td><strong>Appointment Number</strong></td><td>${appointment.appointmentNumber}</td></tr>
                        <tr><td><strong>Status</strong></td><td>
                            <span class="status-badge status-${appointment.status.toLowerCase()}">
                                ${appointment.status}
                            </span>
                        </td></tr>
                        <tr><td><strong>Patient Name</strong></td><td>${appointment.patientName}</td></tr>
                        <tr><td><strong>Contact Number</strong></td><td>${appointment.contactNumber}</td></tr>
                        <tr><td><strong>Dentist</strong></td><td>${appointment.dentistName}</td></tr>
                        <tr><td><strong>Treatment</strong></td><td>${appointment.treatmentType}</td></tr>
                        <tr><td><strong>Treatment Cost</strong></td><td>${formatCurrency(appointment.treatmentCost)}</td></tr>
                        <tr><td><strong>Consultation Fee</strong></td><td>${formatCurrency(appointment.consultationFee)}</td></tr>
                        <tr><td><strong>Date</strong></td><td>${formatDate(appointment.appointmentDate)}</td></tr>
                        <tr><td><strong>Time</strong></td><td>${formatTime(appointment.appointmentTime)}</td></tr>
                        ${appointment.notes ? `<tr><td><strong>Notes</strong></td><td>${appointment.notes}</td></tr>` : ''}
                    </tbody>
                </table>
            </div>
        </div>
        
        <div id="payment-details-container"></div>
        
        <div id="action-buttons" class="card">
            <div class="card-header">
                <h2>Actions</h2>
            </div>
            <div class="action-buttons" style="display: flex; gap: 10px;">
                ${getActionButtons(appointment)}
            </div>
        </div>
    `;
}

/**
 * Display payment details
 */
function displayPaymentDetails(bill) {
    const container = document.getElementById('payment-details-container');

    const statusClass = bill.paymentStatus === 'PAID' ? 'success' : 'warning';

    container.innerHTML = `
        <div class="card">
            <div class="card-header">
                <h2>Payment Information</h2>
            </div>
            
            <div class="payment-status-banner ${statusClass}">
                <h3>
                    <i class="fas ${bill.paymentStatus === 'PAID' ? 'fa-check-circle' : 'fa-clock'}"></i>
                    Payment ${bill.paymentStatus}
                </h3>
            </div>
            
            <div class="table-responsive">
                <table class="table">
                    <tbody>
                        <tr><td><strong>Bill Number</strong></td><td>${bill.billNumber}</td></tr>
                        <tr><td><strong>Treatment Cost</strong></td><td>${formatCurrency(bill.treatmentCost)}</td></tr>
                        <tr><td><strong>Consultation Fee</strong></td><td>${formatCurrency(bill.consultationFee)}</td></tr>
                        ${bill.discount > 0 ? `<tr><td><strong>Discount</strong></td><td>${formatCurrency(bill.discount)}</td></tr>` : ''}
                        <tr><td><strong>Total Amount</strong></td><td><strong>${formatCurrency(bill.totalAmount)}</strong></td></tr>
                        ${bill.paymentMethod ? `<tr><td><strong>Payment Method</strong></td><td>${bill.paymentMethod}</td></tr>` : ''}
                        ${bill.billDate ? `<tr><td><strong>Bill Date</strong></td><td>${formatDate(bill.billDate)}</td></tr>` : ''}
                    </tbody>
                </table>
            </div>
        </div>
    `;
}

/**
 * Display no bill message
 */
function displayNoBill() {
    const container = document.getElementById('payment-details-container');

    container.innerHTML = `
        <div class="card">
            <div class="card-header">
                <h2>Payment Information</h2>
            </div>
            <div class="alert alert-warning">
                <i class="fas fa-info-circle"></i> No bill has been generated for this appointment yet.
            </div>
        </div>
    `;
}

/**
 * Get action buttons based on appointment status
 */
function getActionButtons(appointment) {
    let buttons = '';

    if (appointment.status === 'SCHEDULED') {
        buttons += `
            <button onclick="processBilling('${appointment.appointmentNumber}')" class="btn btn-primary">
                <i class="fas fa-file-invoice"></i> Process Billing
            </button>
            <button onclick="completeAppointment('${appointment.appointmentNumber}')" class="btn btn-success">
                <i class="fas fa-check"></i> Complete
            </button>
            <button onclick="cancelAppointment('${appointment.appointmentNumber}')" class="btn btn-danger">
                <i class="fas fa-times"></i> Cancel
            </button>
        `;
    } else if (appointment.status === 'COMPLETED') {
        buttons += `<p class="text-muted">This appointment is completed.</p>`;
    } else if (appointment.status === 'CANCELLED') {
        buttons += `<p class="text-muted">This appointment is cancelled.</p>`;
    }

    return buttons;
}

/**
 * Process billing
 */
function processBilling(appointmentNumber) {
    window.location.href = `billing.html?appointment=${appointmentNumber}`;
}

/**
 * Complete appointment
 */
async function completeAppointment(appointmentNumber) {
    if (!confirm('Mark this appointment as completed?')) {
        return;
    }

    try {
        const response = await apiPost('/appointments/complete', {
            appointmentNumber: appointmentNumber
        });

        if (response.status === 'SUCCESS') {
            showAlert('details-success', 'Appointment completed successfully', 'success');
            setTimeout(() => location.reload(), 1000);
        }
    } catch (error) {
        showAlert('details-error', error.message, 'error');
    }
}

/**
 * Cancel appointment
 */
async function cancelAppointment(appointmentNumber) {
    if (!confirm('Cancel this appointment?')) {
        return;
    }

    try {
        const response = await apiPost('/appointments/cancel', {
            appointmentNumber: appointmentNumber
        });

        if (response.status === 'SUCCESS') {
            showAlert('details-success', 'Appointment cancelled successfully', 'success');
            setTimeout(() => location.reload(), 1000);
        }
    } catch (error) {
        showAlert('details-error', error.message, 'error');
    }
}