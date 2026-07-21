// =============================================
// login.js - Login page logic
// =============================================

// 1. Show success message if we come from successful registration
// (flash message stored in localStorage from register.js)
const flashMessage = localStorage.getItem('flashMessage');

if (flashMessage) {
    // Show success message
    const successDiv = document.getElementById('success-message');
    successDiv.textContent = flashMessage;

    // Remove the message so it doesn't show again on page reload
    localStorage.removeItem('flashMessage');
}

// 2. Handle form submission
document.getElementById('login-form').addEventListener('submit', async function(e) {

    // Prevent default form submission (page reload)
    e.preventDefault();

    // Clear previous error message
    const errorDiv = document.getElementById('error-message');
    errorDiv.textContent = '';

    // Get form values and trim whitespace
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();

    // Basic client-side validation
    if (!username || !password) {
        errorDiv.textContent = 'Username and password are required';
        return;
    }

    try {
        // Send login request to backend
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userName: username,
                password: password
            })
        });

        // Check if the HTTP status is successful (2xx)
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Login failed');
        }

        // Login successful
        const data = await response.json();

        // Save token
        localStorage.setItem('token', data.token);

        // Redirect to search page
        window.location.href = '/search';

    } catch (error) {
        // Show error message to user
        errorDiv.textContent = error.message;
    }
});