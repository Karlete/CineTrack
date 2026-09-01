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
 * Set of tmdbIds the user has already marked as watched, or an empty set for
 * guests or on failure. Reuses GET /movies/watched instead of a dedicated
 * endpoint (YAGNI). Shared by search.js and movie-detail.js — both need the
 * exact same check, so it lives here instead of being duplicated per page.
 *
 * Uses a raw fetch instead of api.get(): this check is decorative (the page
 * that calls it must keep working without it), but api.get()'s 401 handling
 * force-redirects to /login on ANY failed auth from a non-auth endpoint —
 * exactly what must NOT happen here just because this optional check failed
 * (network error, TMDB/DB hiccup, or an expired token mid-search).
 *
 * The set always holds numbers (tmdbId as it comes from the JSON body).
 * Callers comparing against an id read from the DOM (always a string, e.g.
 * dataset.tmdbId) must convert it with Number(...) first — that is the one
 * accepted criterion, not a per-caller choice.
 */
async function getWatchedIds() {
    const token = localStorage.getItem('token');
    if (!token) {
        return new Set();
    }
    try {
        const response = await fetch('/movies/watched', {
            headers: {'Authorization': `Bearer ${token}`}
        });
        if (!response.ok) {
            return new Set();
        }
        const watched = await response.json();
        return new Set(watched.map(m => m.tmdbId));
    } catch (error) {
        console.warn('No se pudo comprobar el estado de vista:', error);
        return new Set();
    }
}

const WATCHED_LABEL = '✓ Vista';

/**
 * Applies the "already watched" visual state to a mark-watched button.
 * Shared by search.js and movie-detail.js so these two lines don't drift
 * into three slightly different copies again.
 */
function markButtonAsWatched(button) {
    button.textContent = WATCHED_LABEL;
    button.disabled = true;
}

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