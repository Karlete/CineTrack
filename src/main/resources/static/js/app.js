// =============================================
// app.js - Global application logic (navbar, auth state, etc.)
// =============================================

document.addEventListener('DOMContentLoaded', function () {
    const authSection = document.getElementById('auth-section');
    const token = localStorage.getItem('token');

    if (token) {
        // User is logged in
        authSection.innerHTML = `
            <a href="/watched">My Watched</a>
            <a href="#" id="logout-link">Logout</a>
        `;

        // Logout handler
        document.getElementById('logout-link').addEventListener('click', function (e) {
            e.preventDefault(); // Prevent default link behavior (# in URL)
            localStorage.removeItem('token');
            window.location.href = '/login';
        });
    } else {
        // User is not logged in
        authSection.innerHTML = `
            <a href="/login">Login</a>
            <a href="/register">Register</a>
        `;
    }
});