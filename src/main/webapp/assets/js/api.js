/**
 * API utility functions for making HTTP requests
 */

const API_BASE_URL = '/sunrise-dental-clinic/api';

/**
 * Make HTTP request to the API
 */
async function apiRequest(endpoint, method = 'GET', data = null) {
    const url = `${API_BASE_URL}${endpoint}`;

    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'same-origin'
    };

    if (data && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, options);

        // Check if response is JSON
        const contentType = response.headers.get('content-type');
        let responseData;

        if (contentType && contentType.includes('application/json')) {
            responseData = await response.json();
        } else {
            responseData = await response.text();
        }

        // Handle non-OK responses
        if (!response.ok) {
            if (response.status === 401) {
                // Only redirect to login for API calls, not page loads
                if (!window.location.pathname.includes('login.html')) {
                    Auth.clearUserInfo();
                    window.location.href = 'login.html';
                }
                throw new Error('Unauthorized');
            }

            const errorMessage = responseData.message || `Request failed with status ${response.status}`;
            throw new Error(errorMessage);
        }

        return responseData;

    } catch (error) {
        console.error('API Request Error:', error);
        throw error;
    }
}

/**
 * GET request
 */
async function apiGet(endpoint) {
    return apiRequest(endpoint, 'GET');
}

/**
 * POST request
 */
async function apiPost(endpoint, data) {
    return apiRequest(endpoint, 'POST', data);
}

/**
 * PUT request
 */
async function apiPut(endpoint, data) {
    return apiRequest(endpoint, 'PUT', data);
}

/**
 * DELETE request
 */
async function apiDelete(endpoint) {
    return apiRequest(endpoint, 'DELETE');
}

/**
 * Format currency (Sri Lankan Rupees)
 */
function formatCurrency(amount) {
    return `Rs. ${parseFloat(amount).toFixed(2)}`;
}

/**
 * Format date for display
 */
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-GB', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    });
}

/**
 * Format time for display
 */
function formatTime(timeString) {
    if (!timeString) return '';
    const time = new Date(`2000-01-01T${timeString}`);
    return time.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Show alert message
 */
function showAlert(elementId, message, type = 'success') {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.className = `alert alert-${type}`;
        element.style.display = 'block';

        // Auto-hide after 5 seconds
        setTimeout(() => {
            element.style.display = 'none';
        }, 5000);
    }
}

/**
 * Hide alert message
 */
function hideAlert(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.style.display = 'none';
    }
}