// =============================================
// search.js - Movie search page logic
// =============================================

const searchInput = document.getElementById('search-input');
const searchButton = document.getElementById('search-button');
const resultsContainer = document.getElementById('results');
const searchContainer = document.querySelector('.search-container');

// Trigger search when button is clicked or Enter is pressed
searchButton.addEventListener('click', performSearch);

searchInput.addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        performSearch();
    }
});

loadPopularStrip();

/**
 * Fills the empty state (before the user searches anything) with a strip of
 * currently popular movies instead of leaving it blank. Reuses GET
 * /movies/popular (already used on the home page) — no new endpoint.
 */
async function loadPopularStrip() {
    const container = document.getElementById('popular-grid');
    if (!container) return;

    try {
        const movies = await api.get('/movies/popular');
        if (!movies || movies.length === 0) return;

        const watchedIds = await getWatchedIds();

        container.innerHTML = movies
            .map(movie => renderMovieCard(movie, watchedIds.has(movie.tmdbId)))
            .join('');

    } catch (error) {
        console.warn('Error al cargar películas populares:', error);
        // Don't break the search page if TMDB fails — just leave the section empty
    }
}

async function performSearch() {
    const query = searchInput.value.trim();

    document.getElementById('search-placeholder').style.display = 'none';

    if (!query) {
        resultsContainer.innerHTML = '<p class="error">Por favor introduce un término de búsqueda.</p>';
        return;
    }

    resultsContainer.innerHTML = '<p class="loading">Buscando...</p>';

    try {
        // Use the centralized api helper
        const movies = await api.get(`/movies/search?query=${encodeURIComponent(query)}`);

        if (movies.length === 0) {
            resultsContainer.innerHTML = '<p>No se encontraron resultados.</p>';
            return;
        }

        const watchedIds = await getWatchedIds();

        // Render each movie card
        resultsContainer.innerHTML = movies
            .map(movie => renderMovieCard(movie, watchedIds.has(movie.tmdbId)))
            .join('');

    } catch (error) {
        resultsContainer.innerHTML = `<p class="error">${escapeHtml(error.message)}</p>`;
    }
}

function renderMovieCard(movie, isWatched) {
    return `
        <div class="movie-card" data-tmdb-id="${movie.tmdbId}">
            <a class="movie-card-link" href="/movie/${movie.tmdbId}">
                <img src="https://image.tmdb.org/t/p/w200${escapeHtml(movie.posterPath)}"
                     alt="${escapeHtml(movie.title)}"
                     onerror="this.src='/images/no-poster.jpg'">
                <h3>${escapeHtml(movie.title)}</h3>
            </a>
            <p>${movie.year || 'Sin fecha'}</p>
            <button class="mark-watched-btn" data-tmdb-id="${movie.tmdbId}" ${isWatched ? 'disabled' : ''}>
                ${isWatched ? WATCHED_LABEL : 'Marcar como vista'}
            </button>
        </div>
    `;
}

// Event delegation for "Mark as watched" buttons — one listener on the
// shared parent covers both #results (search) and #popular-grid (popular strip)
searchContainer.addEventListener('click', async function (e) {
    const button = e.target.closest('.mark-watched-btn');
    if (!button) return;

    const tmdbId = button.dataset.tmdbId;

    // Check if user is logged in before attempting action
    if (!localStorage.getItem('token')) {
        localStorage.setItem('flashMessage', 'Inicia sesión para marcar películas como vistas.');
        window.location.href = '/login';
        return;
    }

    try {
        await api.post(`/movies/${tmdbId}/watched`);
        markButtonAsWatched(button);

    } catch (error) {
        alert('Error marcando como vista: ' + error.message);
    }
});
