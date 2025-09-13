
function initializeLoginComponent() {
    console.log('Login component initialized');

    setTimeout(() => {
        setupLoginForm();
    }, 50);
}

function setupLoginForm() {
    console.log('Setting up login form');
    const loginBtn = document.getElementById("loginBtn");
    console.log('Login button found:', loginBtn);

    if (loginBtn) {
        const newLoginBtn = loginBtn.cloneNode(true);
        loginBtn.parentNode.replaceChild(newLoginBtn, loginBtn);

        newLoginBtn.addEventListener("click", async function (e) {
            console.log('Login function started');

            const email = document.getElementById('loginEmail').value;
            const password = document.getElementById('loginPassword').value;

            if (!email || !password) {
                alert('Proszę wypełnić wszystkie pola!');
                return;
            }

            const loginData = {
                username: email,
                password: password
            };

            try {
                const response = await fetch('http://localhost:8080/api/users/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(loginData)
                });

                if (response.ok) {
                    const contentType = response.headers.get('content-type');
                    let result;

                    if (contentType && contentType.includes('application/json')) {
                        result = await response.json();
                    } else {
                        result = await response.text();
                    }

                    console.log('Login successful:', result);
                    alert('Logowanie przebiegło pomyślnie!');

                    if (result && result.token) {
                        localStorage.setItem('authToken', result.token);
                    } else {
                        localStorage.setItem('authToken', 'logged_in_' + Date.now());
                    }

                    if (typeof updateNavigation === 'function') {
                        updateNavigation();
                    }
                    if (typeof showAccount === 'function') {
                        showAccount(new Event('click'));
                    }
                } else {
                    const error = await response.text();
                    alert('Błąd logowania: ' + error);
                }
            } catch (error) {
                alert('Błąd połączenia z serwerem: ' + error.message);
            }
        });
    } else {
        console.error('Login button not found!');
    }

    handleLoginFormSubmit();
}

function handleLoginFormSubmit() {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const loginBtn = document.getElementById('loginBtn');
            if (loginBtn) {
                loginBtn.click();
            }
        });
    }
}

function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function clearLoginForm() {
    const emailInput = document.getElementById('loginEmail');
    const passwordInput = document.getElementById('loginPassword');

    if (emailInput) emailInput.value = '';
    if (passwordInput) passwordInput.value = '';
}

if (typeof window !== 'undefined') {
    window.setupLoginForm = setupLoginForm;
    window.initializeLoginComponent = initializeLoginComponent;
    window.handleLoginFormSubmit = handleLoginFormSubmit;
    window.validateEmail = validateEmail;
    window.clearLoginForm = clearLoginForm;
}
