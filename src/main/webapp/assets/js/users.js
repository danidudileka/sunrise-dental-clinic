/**
 * User management functionality (Admin only)
 */

document.addEventListener('DOMContentLoaded', function() {
    // User form handler
    const userForm = document.getElementById('user-form');
    if (userForm) {
        userForm.addEventListener('submit', createUser);
    }
});

/**
 * Toggle dentist select based on role
 */
function toggleDentistSelect() {
    const role = document.getElementById('role').value;
    const dentistGroup = document.getElementById('dentist-select-group');

    if (role === 'DENTIST') {
        dentistGroup.style.display = 'block';
    } else {
        dentistGroup.style.display = 'none';
    }
}

/**
 * Create new staff user
 */
async function createUser(e) {
    e.preventDefault();

    hideAlert('user-success');
    hideAlert('user-error');

    const userData = {
        username: document.getElementById('username').value,
        fullName: document.getElementById('full-name').value,
        email: document.getElementById('email').value,
        role: document.getElementById('role').value,
        dentistId: document.getElementById('dentist-id').value || null
    };

    if (!userData.username || !userData.fullName || !userData.role) {
        showAlert('user-error', 'Please fill in all required fields', 'error');
        return;
    }

    if (userData.role === 'DENTIST' && !userData.dentistId) {
        showAlert('user-error', 'Please select a dentist', 'error');
        return;
    }

    try {
        const response = await apiPost('/users/create-staff', userData);

        if (response.status === 'SUCCESS') {
            // Show credentials in modal
            document.getElementById('modal-username').textContent = response.data.username;
            document.getElementById('modal-password').textContent = response.data.temporaryPassword;
            document.getElementById('password-modal').style.display = 'block';

            // Reset form
            document.getElementById('user-form').reset();
            toggleDentistSelect();
        }

    } catch (error) {
        showAlert('user-error', error.message, 'error');
    }
}

/**
 * Close modal
 */
function closeModal() {
    document.getElementById('password-modal').style.display = 'none';
}