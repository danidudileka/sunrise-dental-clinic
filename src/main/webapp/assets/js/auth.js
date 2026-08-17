/**
 * Authentication management
 */

const Auth = {
    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        // Check both localStorage and sessionStorage
        return localStorage.getItem('userId') !== null ||
            sessionStorage.getItem('userId') !== null;
    },

    /**
     * Get current user info
     */
    getUserInfo() {
        // Try localStorage first, then sessionStorage
        const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
        const username = localStorage.getItem('username') || sessionStorage.getItem('username');
        const fullName = localStorage.getItem('fullName') || sessionStorage.getItem('fullName');
        const role = localStorage.getItem('userRole') || sessionStorage.getItem('userRole');

        return {
            userId: userId,
            username: username,
            fullName: fullName,
            role: role
        };
    },

    /**
     * Set user info after login
     */
    setUserInfo(userInfo) {
        // Store in localStorage (persists across browser sessions)
        localStorage.setItem('userId', userInfo.userId);
        localStorage.setItem('username', userInfo.username);
        localStorage.setItem('fullName', userInfo.fullName);
        localStorage.setItem('userRole', userInfo.role);

        // Also store in sessionStorage (cleared when browser closes)
        sessionStorage.setItem('userId', userInfo.userId);
        sessionStorage.setItem('username', userInfo.username);
        sessionStorage.setItem('fullName', userInfo.fullName);
        sessionStorage.setItem('userRole', userInfo.role);
    },

    /**
     * Clear user info on logout
     */
    clearUserInfo() {
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        localStorage.removeItem('fullName');
        localStorage.removeItem('userRole');

        sessionStorage.removeItem('userId');
        sessionStorage.removeItem('username');
        sessionStorage.removeItem('fullName');
        sessionStorage.removeItem('userRole');
    },

    /**
     * Handle login
     */
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

    /**
     * Handle logout
     */
    async logout() {
        try {
            await apiPost('/auth/logout', {});
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            this.clearUserInfo();
            window.location.href = 'login.html';
        }
    },

    /**
     * Check session validity with server
     */
    async checkSession() {
        try {
            const response = await apiGet('/auth/session');
            return response.data && response.data.authenticated;
        } catch (error) {
            return false;
        }
    }
};

// Login form handler
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('login-form');

    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            // Show loading state
            const buttonText = document.getElementById('login-button-text');
            const buttonSpinner = document.getElementById('login-button-spinner');

            if (buttonText) buttonText.style.display = 'none';
            if (buttonSpinner) buttonSpinner.style.display = 'inline-block';

            try {
                const userInfo = await Auth.login(username, password);

                // Show success message briefly
                showAlert('success-message', 'Login successful! Redirecting...', 'success');

                // Redirect to dashboard after short delay
                setTimeout(() => {
                    window.location.href = 'dashboard.html';
                }, 500);

            } catch (error) {
                showAlert('error-message', error.message, 'error');

                // Hide loading state
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

    // Set user info in navbar
    const userInfo = Auth.getUserInfo();
    if (userInfo.username) {
        const userNameElement = document.getElementById('user-name');
        const welcomeNameElement = document.getElementById('welcome-name');

        if (userNameElement) {
            userNameElement.textContent = userInfo.fullName || userInfo.username;
        }

        if (welcomeNameElement) {
            welcomeNameElement.textContent = userInfo.fullName || userInfo.username;
        }
    }

    // Check authentication on protected pages
    const currentPath = window.location.pathname;
    const isLoginPage = currentPath.includes('login.html');
    const isHelpPage = currentPath.includes('help.html');
    const isErrorPage = currentPath.includes('error.html');
    const isIndexPage = currentPath.endsWith('/') || currentPath.endsWith('/index.html');

    // Only check authentication on protected pages
    if (!isLoginPage && !isHelpPage && !isErrorPage && !isIndexPage) {
        if (!Auth.isAuthenticated()) {
            window.location.href = 'login.html';
        }
    }
});