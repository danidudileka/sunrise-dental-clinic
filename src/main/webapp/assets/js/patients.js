/**
 * Patient management functionality
 */

document.addEventListener('DOMContentLoaded', function() {
    loadAllPatients();

    const patientForm = document.getElementById('patient-form');
    if (patientForm) {
        patientForm.addEventListener('submit', registerPatient);
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
    // Hide all tab contents
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(tab => {
        tab.classList.remove('active');
    });

    // Show selected tab content
    const selectedTab = document.getElementById(`${tabName}-tab`);
    if (selectedTab) {
        selectedTab.classList.add('active');
    }

    // Update active button
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.tab === tabName) {
            btn.classList.add('active');
        }
    });

    // Load data if needed
    if (tabName === 'all') {
        loadAllPatients();
    }
}

/**
 * Register new patient
 */
async function registerPatient(e) {
    e.preventDefault();

    hideAlert('patient-success');
    hideAlert('patient-error');

    const patientData = {
        patientName: document.getElementById('patient-name').value,
        contactNumber: document.getElementById('contact-number').value,
        email: document.getElementById('email').value,
        dateOfBirth: document.getElementById('date-of-birth').value,
        gender: document.getElementById('gender').value,
        bloodGroup: document.getElementById('blood-group').value,
        address: document.getElementById('address').value,
        medicalHistory: document.getElementById('medical-history').value
    };

    if (!patientData.patientName || !patientData.contactNumber) {
        showAlert('patient-error', 'Please fill in required fields', 'error');
        return;
    }

    try {
        const response = await apiPost('/patients/register', patientData);

        if (response.status === 'SUCCESS') {
            document.getElementById('modal-patient-id').textContent = response.data.patientCode;
            document.getElementById('patient-id-modal').style.display = 'block';

            document.getElementById('patient-form').reset();
            loadAllPatients();
        }
    } catch (error) {
        showAlert('patient-error', error.message, 'error');
    }
}

/**
 * Search patient
 */
async function searchPatient() {
    const searchType = document.getElementById('search-type').value;
    const searchTerm = document.getElementById('search-term').value.trim();

    hideAlert('search-error');

    if (!searchTerm) {
        showAlert('search-error', 'Please enter a search term', 'error');
        return;
    }

    try {
        if (searchType === 'code') {
            // Single patient by code - redirect to details page
            window.location.href = `patient-details.html?code=${encodeURIComponent(searchTerm)}`;

        } else if (searchType === 'contact') {
            // Single patient by contact
            const response = await apiGet(`/patients/contact/${encodeURIComponent(searchTerm)}`);
            if (response.status === 'SUCCESS') {
                window.location.href = `patient-details.html?code=${response.data.patientCode}`;
            }

        } else if (searchType === 'name') {
            // Multiple patients by name
            const response = await apiGet(`/patients/search/${encodeURIComponent(searchTerm)}`);
            if (response.status === 'SUCCESS') {
                displayPatientsList(response.data);
            }
        }
    } catch (error) {
        showAlert('search-error', error.message, 'error');
    }
}

/**
 * Display patients list
 */
function displayPatientsList(patients) {
    const allPatientsBody = document.getElementById('all-patients-body');

    if (!patients || patients.length === 0) {
        allPatientsBody.innerHTML = `
            <tr><td colspan="6" class="text-center">No patients found</td></tr>
        `;
        return;
    }

    allPatientsBody.innerHTML = patients.map(patient => `
        <tr>
            <td>${patient.patientCode}</td>
            <td>${patient.patientName}</td>
            <td>${patient.contactNumber}</td>
            <td>${patient.email || 'N/A'}</td>
            <td>${formatDate(patient.createdAt)}</td>
            <td>
                <a href="patient-details.html?code=${patient.patientCode}" 
                   class="btn btn-sm btn-secondary">
                    <i class="fas fa-eye"></i> View
                </a>
            </td>
        </tr>
    `).join('');
}

/**
 * Load all patients
 */
async function loadAllPatients() {
    try {
        const response = await apiGet('/patients/all');

        if (response.status === 'SUCCESS') {
            displayPatientsList(response.data);
        }
    } catch (error) {
        console.error('Error loading patients:', error);
        document.getElementById('all-patients-body').innerHTML = `
            <tr><td colspan="6" class="text-center">Error loading patients</td></tr>
        `;
    }
}

/**
 * Close modal
 */
function closeModal() {
    document.getElementById('patient-id-modal').style.display = 'none';
}