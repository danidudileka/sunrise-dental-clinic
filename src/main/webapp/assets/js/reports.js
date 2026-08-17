/**
 * Reports and analytics functionality
 */

document.addEventListener('DOMContentLoaded', function() {
    // Set default date range (last 7 days)
    const today = new Date();
    const lastWeek = new Date(today);
    lastWeek.setDate(lastWeek.getDate() - 7);

    document.getElementById('start-date').value = lastWeek.toISOString().split('T')[0];
    document.getElementById('end-date').value = today.toISOString().split('T')[0];
});

/**
 * Generate report based on selected type
 */
async function generateReport() {
    const reportType = document.getElementById('report-type').value;
    const startDate = document.getElementById('start-date').value;
    const endDate = document.getElementById('end-date').value;

    if (!startDate || !endDate) {
        showAlert('report-error', 'Please select start and end dates', 'error');
        return;
    }

    if (startDate > endDate) {
        showAlert('report-error', 'Start date cannot be after end date', 'error');
        return;
    }

    // Hide all report sections
    document.querySelectorAll('.report-section').forEach(section => {
        section.style.display = 'none';
    });

    try {
        switch (reportType) {
            case 'summary':
                await generateSummaryReport(startDate, endDate);
                break;

            case 'daily':
                await generateDailyReport(startDate);
                break;

            case 'revenue':
                await generateRevenueReport(startDate, endDate);
                break;
        }

        document.getElementById('report-results').style.display = 'block';

    } catch (error) {
        showAlert('report-error', error.message, 'error');
    }
}

/**
 * Generate summary report
 */
async function generateSummaryReport(startDate, endDate) {
    try {
        const response = await apiGet(`/reports/summary?startDate=${startDate}&endDate=${endDate}`);

        if (response.status === 'SUCCESS') {
            const data = response.data;

            // Update statistics
            document.getElementById('report-total-appointments').textContent = data.totalAppointments || 0;
            document.getElementById('report-total-revenue').textContent = formatCurrency(data.totalRevenue || 0);
            document.getElementById('report-avg-revenue').textContent = formatCurrency(data.averageRevenuePerDay || 0);

            // Display appointments by status
            displayAppointmentsByStatus(data.appointmentsByStatus);

            // Display appointments by dentist
            displayAppointmentsByDentist(data.appointmentsByDentist);

            document.getElementById('summary-report').style.display = 'block';
        }

    } catch (error) {
        console.error('Error generating summary report:', error);
        showAlert('report-error', error.message, 'error');
    }
}

/**
 * Generate daily appointments report
 */
async function generateDailyReport(date) {
    try {
        const response = await apiGet(`/reports/daily?date=${date}`);

        if (response.status === 'SUCCESS') {
            displayDailyAppointments(response.data);
            document.getElementById('daily-report').style.display = 'block';
        }

    } catch (error) {
        console.error('Error generating daily report:', error);
        showAlert('report-error', error.message, 'error');
    }
}

/**
 * Generate revenue report
 */
async function generateRevenueReport(startDate, endDate) {
    try {
        const response = await apiGet(`/reports/revenue?startDate=${startDate}&endDate=${endDate}`);

        if (response.status === 'SUCCESS') {
            displayRevenueReport(response.data);
            document.getElementById('revenue-report').style.display = 'block';
        }

    } catch (error) {
        console.error('Error generating revenue report:', error);
        showAlert('report-error', error.message, 'error');
    }
}

/**
 * Display appointments by status
 */
function displayAppointmentsByStatus(data) {
    const container = document.getElementById('appointments-by-status');

    if (!data || Object.keys(data).length === 0) {
        container.innerHTML = '<p class="text-muted">No data available</p>';
        return;
    }

    let html = '<div class="status-chart">';

    for (const [status, count] of Object.entries(data)) {
        const percentage = calculatePercentage(count, getTotalCount(data));
        const statusColor = getStatusColor(status);

        html += `
            <div class="status-bar">
                <div class="status-label">
                    <span>${status}</span>
                    <span>${count} (${percentage}%)</span>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${percentage}%; background-color: ${statusColor};"></div>
                </div>
            </div>
        `;
    }

    html += '</div>';
    container.innerHTML = html;
}

/**
 * Display appointments by dentist
 */
function displayAppointmentsByDentist(data) {
    const container = document.getElementById('appointments-by-dentist');

    if (!data || Object.keys(data).length === 0) {
        container.innerHTML = '<p class="text-muted">No data available</p>';
        return;
    }

    const maxCount = Math.max(...Object.values(data));

    let html = '<div class="dentist-chart">';

    for (const [dentist, count] of Object.entries(data)) {
        const percentage = (count / maxCount) * 100;

        html += `
            <div class="dentist-bar">
                <div class="dentist-label">
                    <span>${dentist}</span>
                    <span>${count} appointments</span>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${percentage}%;"></div>
                </div>
            </div>
        `;
    }

    html += '</div>';
    container.innerHTML = html;
}

/**
 * Display daily appointments
 */
function displayDailyAppointments(appointments) {
    const tableBody = document.getElementById('daily-report-body');

    if (!appointments || appointments.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center">No appointments for this date</td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = appointments.map(appointment => `
        <tr>
            <td>${appointment.appointmentNumber}</td>
            <td>${appointment.appointmentTime}</td>
            <td>${appointment.patientName}</td>
            <td>${appointment.dentistName}</td>
            <td>${appointment.treatmentName}</td>
            <td>
                <span class="status-badge status-${appointment.status.toLowerCase()}">
                    ${appointment.status}
                </span>
            </td>
        </tr>
    `).join('');
}

/**
 * Display revenue report
 */
function displayRevenueReport(revenueData) {
    const tableBody = document.getElementById('revenue-report-body');

    if (!revenueData || revenueData.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center">No revenue data for this period</td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = revenueData.map(row => `
        <tr>
            <td>${row.billDate}</td>
            <td>${row.totalBills}</td>
            <td>${formatCurrency(row.treatmentRevenue)}</td>
            <td>${formatCurrency(row.consultationRevenue)}</td>
            <td><strong>${formatCurrency(row.totalRevenue)}</strong></td>
        </tr>
    `).join('');
}

/**
 * Calculate percentage
 */
function calculatePercentage(value, total) {
    if (total === 0) return 0;
    return ((value / total) * 100).toFixed(1);
}

/**
 * Get total count from object
 */
function getTotalCount(data) {
    return Object.values(data).reduce((sum, count) => sum + count, 0);
}

/**
 * Get status color
 */
function getStatusColor(status) {
    const colors = {
        'SCHEDULED': '#4299e1',
        'COMPLETED': '#48bb78',
        'CANCELLED': '#f56565',
        'NO_SHOW': '#ed8936'
    };

    return colors[status] || '#a0aec0';
}