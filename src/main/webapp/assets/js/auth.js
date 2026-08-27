/**
 * Authentication management and sidebar navigation
 */

const Auth = {
    isAuthenticated() {
        return localStorage.getItem('userId') !== null ||
            sessionStorage.getItem('userId') !== null;
    },

    getUserInfo() {
        return {
            userId: localStorage.getItem('userId') || sessionStorage.getItem('userId'),
            username: localStorage.getItem('username') || sessionStorage.getItem('username'),
            fullName: localStorage.getItem('fullName') || sessionStorage.getItem('fullName'),
            role: localStorage.getItem('userRole') || sessionStorage.getItem('userRole')
        };
    },

    setUserInfo(userInfo) {
        localStorage.setItem('userId', userInfo.userId);
        localStorage.setItem('username', userInfo.username);
        localStorage.setItem('fullName', userInfo.fullName);
        localStorage.setItem('userRole', userInfo.role);

        sessionStorage.setItem('userId', userInfo.userId);
        sessionStorage.setItem('username', userInfo.username);
        sessionStorage.setItem('fullName', userInfo.fullName);
        sessionStorage.setItem('userRole', userInfo.role);
    },

    clearUserInfo() {
        localStorage.clear();
        sessionStorage.clear();
    },

    hasRole(role) {
        const userRole = this.getUserInfo().role;
        return userRole === role;
    },

    async login(username, password) {
        const response = await apiPost('/auth/login', {
            username: username,
            password: password
        });

        if (response.status === 'SUCCESS') {
            this.setUserInfo(response.data);
            return response.data;
        } else {
            throw new Error(response.message);
        }
    },

    async logout() {
        try {
            await apiPost('/auth/logout', {});
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            this.clearUserInfo();
            window.location.href = 'login.html';
        }
    }
};

/**
 * Initialize sidebar navigation based on user role
 */
function initSidebar() {
    const userInfo = Auth.getUserInfo();
    const sidebarMenu = document.getElementById('sidebar-menu');

    if (!sidebarMenu || !userInfo.role) return;

    // Set user info in sidebar
    const userNameElement = document.getElementById('sidebar-user-name');
    const userRoleElement = document.getElementById('sidebar-user-role');
    const welcomeNameElement = document.getElementById('welcome-name');

    if (userNameElement) userNameElement.textContent = userInfo.fullName || userInfo.username;
    if (userRoleElement) userRoleElement.textContent = userInfo.role;
    if (welcomeNameElement) welcomeNameElement.textContent = userInfo.fullName || userInfo.username;

    // Define menu items based on role
    let menuItems = [];

    if (userInfo.role === 'ADMIN') {
        menuItems = [
            { text: 'Dashboard', icon: 'fa-dashboard', link: 'dashboard.html' },
            { text: 'Patient Management', icon: 'fa-users', link: 'patients.html' },
            { text: 'Appointments', icon: 'fa-calendar', link: 'appointment.html' },
            { text: 'Billing', icon: 'fa-file-invoice', link: 'billing.html' },
            { text: 'Reports', icon: 'fa-chart-line', link: 'reports.html' },
            { text: 'User Management', icon: 'fa-user-cog', link: 'users.html' },
            { text: 'Help', icon: 'fa-question-circle', link: 'help.html' }
        ];
    } else if (userInfo.role === 'RECEPTIONIST') {
        menuItems = [
            { text: 'Dashboard', icon: 'fa-dashboard', link: 'dashboard.html' },
            { text: 'Patient Management', icon: 'fa-users', link: 'patients.html' },
            { text: 'Appointments', icon: 'fa-calendar', link: 'appointment.html' },
            { text: 'Billing', icon: 'fa-file-invoice', link: 'billing.html' },
            { text: 'Reports', icon: 'fa-chart-line', link: 'reports.html' },
            { text: 'Help', icon: 'fa-question-circle', link: 'help.html' }
        ];
    } else if (userInfo.role === 'DENTIST') {
        menuItems = [
            { text: 'My Dashboard', icon: 'fa-dashboard', link: 'doctor-dashboard.html' },
            { text: 'My Appointments', icon: 'fa-calendar', link: 'doctor-dashboard.html' },
            { text: 'Patient Search', icon: 'fa-search', link: 'patients.html' },
            { text: 'Help', icon: 'fa-question-circle', link: 'help.html' }
        ];
    }

    // Generate menu HTML
    const currentPage = window.location.pathname.split('/').pop();

    sidebarMenu.innerHTML = menuItems.map(item => {
        const isActive = item.link === currentPage;
        return `
            <li>
                <a href="${item.link}" class="${isActive ? 'active' : ''}">
                    <span class="icon"><i class="fas ${item.icon}"></i></span>
                    <span>${item.text}</span>
                </a>
            </li>
        `;
    }).join('');

    // Redirect doctor to doctor dashboard if on wrong page
    if (userInfo.role === 'DENTIST' &&
        !currentPage.includes('doctor-dashboard') &&
        !currentPage.includes('patients') &&
        !currentPage.includes('help') &&
        !currentPage.includes('login')) {
        window.location.href = 'doctor-dashboard.html';
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Login form handler
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            const buttonText = document.getElementById('login-button-text');
            const buttonSpinner = document.getElementById('login-button-spinner');

            if (buttonText) buttonText.style.display = 'none';
            if (buttonSpinner) buttonSpinner.style.display = 'inline-block';

            try {
                const userInfo = await Auth.login(username, password);

                showAlert('success-message', 'Login successful! Redirecting...', 'success');

                setTimeout(() => {
                    if (userInfo.role === 'DENTIST') {
                        window.location.href = 'doctor-dashboard.html';
                    } else {
                        window.location.href = 'dashboard.html';
                    }
                }, 500);

            } catch (error) {
                showAlert('error-message', error.message, 'error');
                if (buttonText) buttonText.style.display = 'inline';
                if (buttonSpinner) buttonSpinner.style.display = 'none';
            }
        });
    }

    // Logout button handler
    const logoutButton = document.getElementById('logout-button');
    if (logoutButton) {
        logoutButton.addEventListener('click', function(e) {
            e.preventDefault();
            Auth.logout();
        });
    }

    // Initialize sidebar if not on login page
    if (!window.location.pathname.includes('login.html')) {
        initSidebar();
    }

    // Check authentication on protected pages
    const currentPath = window.location.pathname;
    const isLoginPage = currentPath.includes('login.html');
    const isHelpPage = currentPath.includes('help.html');
    const isErrorPage = currentPath.includes('error.html');

    if (!isLoginPage && !isHelpPage && !isErrorPage) {
        if (!Auth.isAuthenticated()) {
            window.location.href = 'login.html';
        }
    }
});