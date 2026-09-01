// =============================================
// movie-detail.js - Movie detail page logic
// Wires the "mark as watched" button state.
// =============================================

document.addEventListener('DOMContentLoaded', async function () {
    const button = document.querySelector('.mark-watched-btn');
    if (!button) return;

    const tmdbId = button.dataset.tmdbId;

    // If already watched, reflect that on load. Decision (deliberate, not an
    // oversight): the button only ever moves toward "watched" here, same as
    // search.js — there is no way to unmark from the detail page. Unmarking
    // stays exclusive to /watched, which already has that DELETE flow. YAGNI
    // for v1.0; revisit if users ask for it from the detail page directly.
    //
    // Set contents are always numbers (see getWatchedIds in api.js), so a
    // dataset-sourced id — always a string — is converted with Number(...)
    // before the lookup. Same criterion api.js documents for every caller.
    const watchedIds = await getWatchedIds();
    if (watchedIds.has(Number(tmdbId))) {
        markButtonAsWatched(button);
    }

    button.addEventListener('click', async function () {
        // Same behavior as search.js: no token means redirect to login
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
});
