
function initializeResetPasswordComponent() {
    console.log('Reset password component initialized');

    setTimeout(() => {
        setupResetPasswordForm();
    }, 50);
}

function setupResetPasswordForm() {
    console.log('Setting up reset password form');
    const resetPasswordBtn = document.getElementById("resetPasswordBtn");
    console.log('Reset password button found:', resetPasswordBtn);

    if (resetPasswordBtn) {
        const newResetPasswordBtn = resetPasswordBtn.cloneNode(true);
        resetPasswordBtn.parentNode.replaceChild(newResetPasswordBtn, resetPasswordBtn);

        newResetPasswordBtn.addEventListener("click", async function (e) {
            e.preventDefault();
            console.log('Reset password function started');

            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            // Validation
            if (!newPassword || !confirmPassword) {
                showMessage('Proszę wypełnić wszystkie pola!', 'error');
                return;
            }

            if (newPassword.length < 8) {
                showMessage('Hasło musi mieć minimum 8 znaków!', 'error');
                return;
            }

            if (newPassword !== confirmPassword) {
                showMessage('Hasła nie są identyczne!', 'error');
                return;
            }

            // Get token from URL
            const urlParams = new URLSearchParams(window.location.search);
            const token = urlParams.get('token');

            if (!token) {
                showMessage('Brak tokenu resetującego. Link jest nieprawidłowy.', 'error');
                return;
            }

            // Disable button during request
            newResetPasswordBtn.disabled = true;
            newResetPasswordBtn.textContent = 'Resetowanie...';

            try {
                const response = await fetch(`${API_CONFIG.baseUrl}/api/users/reset-password?token=${encodeURIComponent(token)}&newPassword=${encodeURIComponent(newPassword)}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                if (response.ok) {
                    console.log('Password reset successful');
                    showMessage('Hasło zostało pomyślnie zresetowane! Za chwilę zostaniesz przekierowany do logowania...', 'success');

                    // Clear form
                    document.getElementById('newPassword').value = '';
                    document.getElementById('confirmPassword').value = '';

                    // Redirect to login after 3 seconds
                    setTimeout(() => {
                        // Remove token from URL and redirect to login
                        window.history.replaceState({}, document.title, window.location.pathname);
                        if (typeof showLogin === 'function') {
                            showLogin(new Event('click'));
                        }
                    }, 3000);
                } else {
                    const error = await response.json();
                    const errorMessage = error.message || 'Wystąpił błąd podczas resetowania hasła.';
                    showMessage(errorMessage, 'error');
                }
            } catch (error) {
                console.error('Error:', error);
                showMessage('Błąd połączenia z serwerem: ' + error.message, 'error');
            } finally {
                // Re-enable button
                newResetPasswordBtn.disabled = false;
                newResetPasswordBtn.textContent = 'Zmień hasło';
            }
        });
    } else {
        console.error('Reset password button not found!');
    }

    handleResetPasswordFormSubmit();
}

function handleResetPasswordFormSubmit() {
    const resetPasswordForm = document.getElementById('resetPasswordForm');
    if (resetPasswordForm) {
        resetPasswordForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const resetPasswordBtn = document.getElementById('resetPasswordBtn');
            if (resetPasswordBtn) {
                resetPasswordBtn.click();
            }
        });
    }
}

function showMessage(message, type) {
    // Remove existing messages
    const existingMessages = document.querySelectorAll('.success-message, .error-message');
    existingMessages.forEach(msg => msg.remove());

    // Create new message
    const messageDiv = document.createElement('div');
    messageDiv.className = type === 'success' ? 'success-message' : 'error-message';
    messageDiv.textContent = message;

    // Insert message before the form
    const form = document.getElementById('resetPasswordForm');
    if (form) {
        form.parentNode.insertBefore(messageDiv, form);
    }
}

if (typeof window !== 'undefined') {
    window.setupResetPasswordForm = setupResetPasswordForm;
    window.initializeResetPasswordComponent = initializeResetPasswordComponent;
}
