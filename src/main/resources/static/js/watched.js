// =============================================
// watched.js - Watched movies page logic
// =============================================

const resultsContainer = document.getElementById('results');

// Load watched movies when the page loads
document.addEventListener('DOMContentLoaded', loadWatchedMovies);

async function loadWatchedMovies() {
    // Check if user is logged in before attempting to load watched movies
    if (!localStorage.getItem('token')) {
        localStorage.setItem('flashMessage', 'Necesitas hacer login para ver tus películas vistas.');
        window.location.href = '/login';
        return;
    }

    resultsContainer.innerHTML = '<p class="loading">Cargando tus películas vistas...</p>';

    try {
        const movies = await api.get('/movies/watched');

        if (movies.length === 0) {
            resultsContainer.innerHTML = '<p>No has marcado ninguna película como vista.</p>';
            return;
        }

        // Render each watched movie card
        resultsContainer.innerHTML = movies.map(movie => `
            <div class="movie-card" data-tmdb-id="${movie.tmdbId}">
                <a class="movie-card-link" href="/movie/${movie.tmdbId}">
                    <img src="https://image.tmdb.org/t/p/w200${escapeHtml(movie.posterPath)}"
                         alt="${escapeHtml(movie.title)}"
                         onerror="this.src='/images/no-poster.jpg'">
                    <h3>${escapeHtml(movie.title)}</h3>
                </a>
                <p>${movie.year || 'Sin fecha'}</p>
                <p class="watched-date">Vista el: ${new Date(movie.watchedAt).toLocaleDateString()}</p>
                <button class="remove-watched-btn" data-tmdb-id="${movie.tmdbId}">
                    Quitar de vistas
                </button>
            </div>
        `).join('');

    } catch (error) {
        resultsContainer.innerHTML = `<p class="error">${escapeHtml(error.message)}</p>`;
    }
}

// Event delegation for "Remove from watched" buttons
resultsContainer.addEventListener('click', async function (e) {
    const button = e.target.closest('.remove-watched-btn');
    if (!button) return;

    const tmdbId = button.dataset.tmdbId;

    try {
        await api.delete(`/movies/${tmdbId}/watched`);

        // Remove the card from DOM after successful delete
        button.closest('.movie-card').remove();

        // If no cards left, show empty message
        if (resultsContainer.children.length === 0) {
            resultsContainer.innerHTML = '<p>No has marcado ninguna película como vista.</p>';
        }

    } catch (error) {
        alert('Error al quitar de vistas: ' + error.message);
    }
});