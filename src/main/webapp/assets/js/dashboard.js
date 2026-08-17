document.addEventListener('DOMContentLoaded', function() {
    // Check if user is authenticated
    if (!Auth.isAuthenticated()) {
        window.location.href = 'login.html';
        return;
    }

    // Load dashboard data
    loadDashboardSummary();
});

async function loadDashboardSummary() {
    try {
        const response = await apiGet('/reports/dashboard');

        if (response.status === 'SUCCESS') {
            updateDashboardStats(response.data);
        }

    } catch (error) {
        console.error('Error loading dashboard:', error);

        // If unauthorized, redirect to login
        if (error.message === 'Unauthorized') {
            window.location.href = 'login.html';
        }
    }
}

/**
 * Update dashboard statistics
 */
function updateDashboardStats(data) {
    const todayAppointments = document.getElementById('today-appointments');
    const weekAppointments = document.getElementById('week-appointments');
    const totalPatients = document.getElementById('total-patients');
    const todayRevenue = document.getElementById('today-revenue');
    const monthRevenue = document.getElementById('month-revenue');

    if (todayAppointments) {
        todayAppointments.textContent = data.todayAppointments || 0;
    }

    if (weekAppointments) {
        weekAppointments.textContent = data.weekAppointments || 0;
    }

    if (totalPatients) {
        totalPatients.textContent = data.totalPatients || 0;
    }

    if (todayRevenue) {
        todayRevenue.textContent = formatCurrency(data.todayRevenue || 0);
    }

    if (monthRevenue) {
        monthRevenue.textContent = formatCurrency(data.monthRevenue || 0);
    }
}