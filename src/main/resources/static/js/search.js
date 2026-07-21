// =============================================
// search.js - Movie search page logic
// =============================================

const searchInput = document.getElementById('search-input');
const searchButton = document.getElementById('search-button');
const resultsContainer = document.getElementById('results');

// Trigger search when button is clicked or Enter is pressed
searchButton.addEventListener('click', performSearch);

searchInput.addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        performSearch();
    }
});

async function performSearch() {
    const query = searchInput.value.trim();

    if (!query) {
        resultsContainer.innerHTML = '<p class="error">Please enter a search term.</p>';
        return;
    }

    resultsContainer.innerHTML = '<p class="loading">Searching...</p>';

    try {
        // Use the centralized api helper
        const movies = await api.get(`/movies/search?query=${encodeURIComponent(query)}`);

        if (movies.length === 0) {
            resultsContainer.innerHTML = '<p>No results found.</p>';
            return;
        }

        // Render each movie card
        resultsContainer.innerHTML = movies.map(movie => `
            <div class="movie-card" data-tmdb-id="${movie.tmdbId}">
                <img src="https://image.tmdb.org/t/p/w200${movie.posterPath}" 
                     alt="${movie.title}" 
                     onerror="this.src='/images/no-poster.jpg'">
                <h3>${movie.title}</h3>
                <p>${movie.year || 'No date'}</p>
                <button class="mark-watched-btn" data-tmdb-id="${movie.tmdbId}">
                    Mark as watched
                </button>
            </div>
        `).join('');

    } catch (error) {
        resultsContainer.innerHTML = `<p class="error">${error.message}</p>`;
    }
}

// Event delegation for "Mark as watched" buttons
resultsContainer.addEventListener('click', async function (e) {
    const button = e.target.closest('.mark-watched-btn');
    if (!button) return;

    const tmdbId = button.dataset.tmdbId;

    // Check if user is logged in before attempting action
    if (!localStorage.getItem('token')) {
        localStorage.setItem('flashMessage', 'You need to log in to save movies.');
        window.location.href = '/login';
        return;
    }

    try {
        await api.post(`/movies/${tmdbId}/watched`);

        // Update button UI
        button.textContent = '✓ Watched';
        button.disabled = true;

    } catch (error) {
        alert('Error marking as watched: ' + error.message);
    }
});