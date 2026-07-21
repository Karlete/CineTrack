// =============================================
// api.js - Centralized API calls with authentication
// =============================================

const api = {
    /**
     * Makes a GET request with JWT if available
     */
    async get(url) {
        const token = localStorage.getItem('token');
        const headers = token ? {'Authorization': `Bearer ${token}`} : {};

        const response = await fetch(url, {headers});
        return handleResponse(response);
    },

    /**
     * Makes a POST request with JWT if available
     */
    async post(url, body) {
        const token = localStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json',
            ...(token && {'Authorization': `Bearer ${token}`})
        };

        const response = await fetch(url, {
            method: 'POST',
            headers,
            body: JSON.stringify(body)
        });
        return handleResponse(response);
    },

    /**
     * Makes a DELETE request with JWT if available
     */
    async delete(url) {
        const token = localStorage.getItem('token');
        const headers = token ? {'Authorization': `Bearer ${token}`} : {};

        const response = await fetch(url, {
            method: 'DELETE',
            headers
        });
        return handleResponse(response);
    }
};

/**
 * Helper to handle responses consistently
 */
async function handleResponse(response) {
    if (response.status === 401) {
        // Special case for auth endpoints (login/register) - do not auto-redirect
        if (!response.url.includes('/auth/')) {
            localStorage.removeItem('token');
            localStorage.setItem('flashMessage', 'Your session has expired. Please log in again.');
            window.location.href = '/login';
        }
        // For auth endpoints, let the caller handle the error
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Authentication failed');
    }

    if (!response.ok) {
        let errorMessage = 'Request failed';
        try {
            const errorData = await response.json();
            errorMessage = errorData.message || errorMessage;
        } catch (e) {
            // Ignore if can't parse JSON
        }
        throw new Error(errorMessage);
    }

    return response.json();
}