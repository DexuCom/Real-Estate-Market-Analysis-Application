
function initializeForgotPasswordComponent() {
    console.log('Forgot password component initialized');

    setTimeout(() => {
        setupForgotPasswordForm();
    }, 50);
}

function setupForgotPasswordForm() {
    console.log('Setting up forgot password form');
    const forgotPasswordBtn = document.getElementById("forgotPasswordBtn");
    console.log('Forgot password button found:', forgotPasswordBtn);

    if (forgotPasswordBtn) {
        const newForgotPasswordBtn = forgotPasswordBtn.cloneNode(true);
        forgotPasswordBtn.parentNode.replaceChild(newForgotPasswordBtn, forgotPasswordBtn);

        newForgotPasswordBtn.addEventListener("click", async function (e) {
            e.preventDefault();
            console.log('Forgot password function started');

            const email = document.getElementById('forgotEmail').value;

            if (!email) {
                showMessage('Proszę podać adres email!', 'error');
                return;
            }

            if (!validateEmail(email)) {
                showMessage('Proszę podać prawidłowy adres email!', 'error');
                return;
            }

            newForgotPasswordBtn.disabled = true;
            newForgotPasswordBtn.textContent = 'Wysyłanie...';

            try {
                const response = await fetch(`${API_CONFIG.baseUrl}/api/users/forgot-password?email=${encodeURIComponent(email)}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                if (response.ok) {
                    console.log('Forgot password request successful');
                    showMessage('Link do resetowania hasła został wysłany na podany adres email. Sprawdź swoją skrzynkę!', 'success');

                    document.getElementById('forgotEmail').value = '';

                    setTimeout(() => {
                        if (typeof showLogin === 'function') {
                            showLogin(new Event('click'));
                        }
                    }, 3000);
                } else {
                    const error = await response.json();
                    const errorMessage = error.message || 'Wystąpił błąd podczas wysyłania linku resetującego.';
                    showMessage(errorMessage, 'error');
                }
            } catch (error) {
                console.error('Error:', error);
                showMessage('Błąd połączenia z serwerem: ' + error.message, 'error');
            } finally {
                newForgotPasswordBtn.disabled = false;
                newForgotPasswordBtn.textContent = 'Wyślij link do resetu hasła';
            }
        });
    } else {
        console.error('Forgot password button not found!');
    }

    handleForgotPasswordFormSubmit();
}

function handleForgotPasswordFormSubmit() {
    const forgotPasswordForm = document.getElementById('forgotPasswordForm');
    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const forgotPasswordBtn = document.getElementById('forgotPasswordBtn');
            if (forgotPasswordBtn) {
                forgotPasswordBtn.click();
            }
        });
    }
}

function showMessage(message, type) {
    const existingMessages = document.querySelectorAll('.success-message, .error-message');
    existingMessages.forEach(msg => msg.remove());

    const messageDiv = document.createElement('div');
    messageDiv.className = type === 'success' ? 'success-message' : 'error-message';
    messageDiv.textContent = message;

    const form = document.getElementById('forgotPasswordForm');
    if (form) {
        form.parentNode.insertBefore(messageDiv, form);
    }
}

function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

if (typeof window !== 'undefined') {
    window.setupForgotPasswordForm = setupForgotPasswordForm;
    window.initializeForgotPasswordComponent = initializeForgotPasswordComponent;
}
