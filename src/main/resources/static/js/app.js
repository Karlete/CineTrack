// =============================================
// app.js - Global application logic
// Navbar, auth state, greeting, recent watched, etc.
// =============================================

document.addEventListener('DOMContentLoaded', function () {
    const token = localStorage.getItem('token');

    // ==================== NAVBAR ====================
    const authSection = document.getElementById('auth-section');
    if (authSection) {
        if (token) {
            // User is logged in
            authSection.innerHTML = `
                <a href="/watched">My Watched</a>
                <a href="#" id="logout-link">Logout</a>
            `;

            // Logout handler
            document.getElementById('logout-link').addEventListener('click', function (e) {
                e.preventDefault();
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
    }

    // ==================== HOME PAGE ONLY ====================
    const heroGuest = document.getElementById('hero-guest');
    const heroUser = document.getElementById('hero-user');
    const howItWorks = document.getElementById('how-it-works');
    const recentWatched = document.getElementById('recent-watched');

    if (heroGuest && heroUser) {
        if (token) {
            const username = getUsernameFromToken(token);

            if (!username) {
                // Token is corrupted or invalid
                localStorage.removeItem('token');
                heroGuest.style.display = 'block';
                heroUser.style.display = 'none';
                if (recentWatched) recentWatched.style.display = 'none';
                return;
            }

            // Valid logged in user
            heroGuest.style.display = 'none';
            heroUser.style.display = 'block';
            document.getElementById('hero-greeting').textContent = `¡Hola de nuevo, ${username}!`;

            // Show recent watched section
            if (recentWatched) {
                recentWatched.style.display = 'block';
                loadRecentWatched();
            }

            // Hide how it works for logged in users
            if (howItWorks) howItWorks.style.display = 'none';
        } else {
            // Not logged in
            heroUser.style.display = 'none';
            if (recentWatched) recentWatched.style.display = 'none';
        }
    }
});

/**
 * Decodes the username from JWT token (payload.sub)
 * Returns null if token is invalid or missing
 */
function getUsernameFromToken(token) {
    try {
        const payload = token.split('.')[1];
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
        const decoded = JSON.parse(atob(base64));
        return decoded.sub || null;
    } catch (e) {
        return null;
    }
}

/**
 * Loads and renders the recent watched movies on the home page
 */
async function loadRecentWatched() {
    const container = document.getElementById('recent-watched-grid');
    if (!container) return;

    try {
        const movies = await api.get('/movies/watched');

        if (movies.length === 0) {
            container.innerHTML = '<p>Aún no has marcado ninguna película como vista.</p>';
            return;
        }

        // Sort by watchedAt descending and take the latest 5
        const recent = [...movies]
            .sort((a, b) => new Date(b.watchedAt) - new Date(a.watchedAt))
            .slice(0, 5);

        container.innerHTML = recent.map(movie => `
            <div class="movie-card" data-tmdb-id="${movie.tmdbId}">
                <img src="https://image.tmdb.org/t/p/w200${movie.posterPath}" 
                     alt="${movie.title}" 
                     onerror="this.src='/images/no-poster.jpg'">
                <h3>${movie.title}</h3>
                <p>${movie.year || 'Sin fecha'}</p>
            </div>
        `).join('');

    } catch (error) {
        console.warn('Failed to load recent watched movies:', error);
        container.innerHTML = '<p>No se pudieron cargar las películas recientes.</p>';
    }
}