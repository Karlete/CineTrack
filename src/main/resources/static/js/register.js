document.getElementById('register-form').addEventListener('submit', async function(e) {
    e.preventDefault();

    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();
    const errorDiv = document.getElementById('error-message');

    errorDiv.textContent = '';

    if (!username || !email || !password) {
        errorDiv.textContent = 'Todos los campos son obligatorios';
        return;
    }

    try {
        const response = await fetch('/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userName: username,
                email: email,
                password: password
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Error en registro');
        }

        // Register OK save message
        localStorage.setItem('flashMessage', 'Registro exitoso. Ahora puedes iniciar sesión.');
        window.location.href = '/login';

    } catch (error) {
        errorDiv.textContent = error.message;
    }
});