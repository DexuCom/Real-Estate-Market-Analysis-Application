
function initializeRegisterComponent() {
    console.log('Register component initialized');

    setTimeout(() => {
        setupRegisterForm();
    }, 50);
}

function setupRegisterForm() {
    console.log('Setting up register form');
    const registerBtn = document.getElementById("registerBtn");
    console.log('Register button found:', registerBtn);

    if (registerBtn) {
        const newRegisterBtn = registerBtn.cloneNode(true);
        registerBtn.parentNode.replaceChild(newRegisterBtn, registerBtn);

        newRegisterBtn.addEventListener("click", async function (e) {
            console.log('Register function started');

            const username = document.getElementById('username').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const termsAccepted = document.getElementById('terms').checked;

            if (!username || !email || !password || !confirmPassword) {
                alert('Proszę wypełnić wszystkie pola!');
                return;
            }

            if (!validateEmail(email)) {
                alert('Proszę podać prawidłowy adres email!');
                return;
            }

            if (password.length < 6) {
                alert('Hasło musi zawierać co najmniej 6 znaków!');
                return;
            }

            if (password !== confirmPassword) {
                alert('Hasła nie są identyczne!');
                return;
            }

            if (!termsAccepted) {
                alert('Musisz zaakceptować regulamin i politykę prywatności!');
                return;
            }

            const userData = {
                username: username,
                password: password,
                email: email
            };

            try {
                const response = await fetch(`${API_CONFIG.baseUrl}/api/users`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(userData)
                });

                if (response.ok) {
                    const contentType = response.headers.get('content-type');
                    let result;

                    if (contentType && contentType.includes('application/json')) {
                        result = await response.json();
                    } else {
                        result = await response.text();
                    }

                    console.log('Registration successful:', result);
                    alert('Rejestracja przebiegła pomyślnie! Teraz możesz się zalogować.');

                    clearRegisterForm();

                    if (typeof showLogin === 'function') {
                        showLogin(new Event('click'));
                    }
                } else {
                    const error = await response.text();
                    alert('Błąd rejestracji: ' + error);
                }
            } catch (error) {
                alert('Błąd połączenia z serwerem: ' + error.message);
            }
        });
    } else {
        console.error('Register button not found!');
    }

    handleRegisterFormSubmit();

    setupPasswordValidation();
}

function handleRegisterFormSubmit() {
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const registerBtn = document.getElementById('registerBtn');
            if (registerBtn) {
                registerBtn.click();
            }
        });
    }
}

function setupPasswordValidation() {
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    if (passwordInput && confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', function () {
            const password = passwordInput.value;
            const confirmPassword = confirmPasswordInput.value;

            if (confirmPassword && password !== confirmPassword) {
                confirmPasswordInput.setCustomValidity('Hasła nie są identyczne');
                confirmPasswordInput.style.borderColor = '#ff4444';
            } else {
                confirmPasswordInput.setCustomValidity('');
                confirmPasswordInput.style.borderColor = '';
            }
        });

        passwordInput.addEventListener('input', function () {
            const password = passwordInput.value;
            const confirmPassword = confirmPasswordInput.value;

            if (confirmPassword && password !== confirmPassword) {
                confirmPasswordInput.setCustomValidity('Hasła nie są identyczne');
                confirmPasswordInput.style.borderColor = '#ff4444';
            } else {
                confirmPasswordInput.setCustomValidity('');
                confirmPasswordInput.style.borderColor = '';
            }
        });
    }
}

function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function validatePasswordStrength(password) {
    if (password.length < 6) {
        return { valid: false, message: 'Hasło musi mieć co najmniej 6 znaków' };
    }
    if (!/(?=.*[a-z])/.test(password)) {
        return { valid: false, message: 'Hasło musi zawierać co najmniej jedną małą literę' };
    }
    if (!/(?=.*[A-Z])/.test(password)) {
        return { valid: false, message: 'Hasło musi zawierać co najmniej jedną wielką literę' };
    }
    if (!/(?=.*\d)/.test(password)) {
        return { valid: false, message: 'Hasło musi zawierać co najmniej jedną cyfrę' };
    }
    return { valid: true, message: 'Hasło jest wystarczająco silne' };
}

function clearRegisterForm() {
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const termsCheckbox = document.getElementById('terms');

    if (usernameInput) usernameInput.value = '';
    if (emailInput) emailInput.value = '';
    if (passwordInput) passwordInput.value = '';
    if (confirmPasswordInput) confirmPasswordInput.value = '';
    if (termsCheckbox) termsCheckbox.checked = false;
}

if (typeof window !== 'undefined') {
    window.setupRegisterForm = setupRegisterForm;
    window.initializeRegisterComponent = initializeRegisterComponent;
    window.handleRegisterFormSubmit = handleRegisterFormSubmit;
    window.setupPasswordValidation = setupPasswordValidation;
    window.validateEmail = validateEmail;
    window.validatePasswordStrength = validatePasswordStrength;
    window.clearRegisterForm = clearRegisterForm;
}
