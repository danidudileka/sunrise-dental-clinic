/**
 * Client-side validation utilities
 */

/**
 * Validate phone number
 */
function validatePhoneNumber(phone) {
    const phoneRegex = /^\+?[0-9\-\s]{10,20}$/;
    return phoneRegex.test(phone);
}

/**
 * Validate email
 */
function validateEmail(email) {
    if (!email) return true; // Email is optional
    const emailRegex = /^[A-Za-z0-9+_.-]+@(.+)$/;
    return emailRegex.test(email);
}

/**
 * Validate name
 */
function validateName(name) {
    const nameRegex = /^[a-zA-Z\s]{2,100}$/;
    return nameRegex.test(name);
}

/**
 * Validate appointment number
 */
function validateAppointmentNumber(number) {
    const appointmentRegex = /^APT[0-9]{9}$/;
    return appointmentRegex.test(number);
}

/**
 * Validate date
 */
function validateDate(dateStr) {
    if (!dateStr) return false;

    const date = new Date(dateStr);
    return !isNaN(date.getTime());
}

/**
 * Validate future date
 */
function validateFutureDate(dateStr) {
    if (!validateDate(dateStr)) return false;

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const date = new Date(dateStr);
    return date >= today;
}

/**
 * Validate time
 */
function validateTime(timeStr) {
    if (!timeStr) return false;

    const timeRegex = /^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$/;
    return timeRegex.test(timeStr);
}

/**
 * Validate business hours (8 AM - 8 PM)
 */
function validateBusinessHours(timeStr) {
    if (!validateTime(timeStr)) return false;

    const time = new Date(`2000-01-01T${timeStr}`);
    const startHour = 8;
    const endHour = 20;

    const hour = time.getHours();
    return hour >= startHour && hour <= endHour;
}

/**
 * Show field validation error
 */
function showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    if (!field) return;

    field.classList.add('is-invalid');

    // Create or update error message
    let errorElement = field.nextElementSibling;
    if (!errorElement || !errorElement.classList.contains('field-error')) {
        errorElement = document.createElement('small');
        errorElement.className = 'field-error';
        field.parentNode.insertBefore(errorElement, field.nextElementSibling);
    }

    errorElement.textContent = message;
}

/**
 * Clear field validation error
 */
function clearFieldError(fieldId) {
    const field = document.getElementById(fieldId);
    if (!field) return;

    field.classList.remove('is-invalid');

    const errorElement = field.nextElementSibling;
    if (errorElement && errorElement.classList.contains('field-error')) {
        errorElement.remove();
    }
}

/**
 * Validate entire form
 */
function validateForm(formData, validationRules) {
    const errors = {};

    for (const [field, rules] of Object.entries(validationRules)) {
        const value = formData[field];

        for (const rule of rules) {
            if (!rule.validator(value)) {
                errors[field] = rule.message;
                break;
            }
        }
    }

    return errors;
}

/**
 * Common validation rules
 */
const validationRules = {
    required: (message = 'This field is required') => ({
        validator: (value) => value !== null && value !== undefined && value.toString().trim() !== '',
        message: message
    }),

    phone: (message = 'Invalid phone number') => ({
        validator: (value) => validatePhoneNumber(value),
        message: message
    }),

    email: (message = 'Invalid email address') => ({
        validator: (value) => validateEmail(value),
        message: message
    }),

    name: (message = 'Invalid name (letters and spaces only)') => ({
        validator: (value) => validateName(value),
        message: message
    }),

    futureDate: (message = 'Date must be today or in the future') => ({
        validator: (value) => validateFutureDate(value),
        message: message
    }),

    time: (message = 'Invalid time format') => ({
        validator: (value) => validateTime(value),
        message: message
    })
};