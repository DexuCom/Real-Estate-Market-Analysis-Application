
function initializeAccountComponent() {
    console.log('Account component initialized');

    const authToken = localStorage.getItem('authToken');
    if (authToken) {
        loadUserData();
        loadWatchlistProperties();
    }
}

function loadUserData() {
    console.log('Loading user data');

    const userId = 'ba6e97db-1f6f-41fc-a80a-5f948f0ace8c';

    fetch(`http://localhost:8080/api/users/${userId}`)
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Failed to load user data');
            }
        })
        .then(userData => {
            console.log('User data loaded:', userData);
            updateUserInterface(userData);
        })
        .catch(error => {
            console.error('Error loading user data:', error);
            localStorage.removeItem('authToken');
            updateNavigation();
            showAccount(new Event('click'));
        });
}

function updateUserInterface(userData) {
    const usernameDisplay = document.querySelector('.username');
    if (usernameDisplay) {
        usernameDisplay.textContent = userData.email;
    }

    const avatarLetter = document.querySelector('.avatar-letter');
    if (avatarLetter && userData.username) {
        avatarLetter.textContent = userData.username.charAt(0).toUpperCase();
    }
}

function loadWatchlistProperties() {
    console.log('Loading watchlist properties');

    const propertiesListElement = document.getElementById('properties-list');
    console.log('Properties list element found:', propertiesListElement);

    const userId = 'ba6e97db-1f6f-41fc-a80a-5f948f0ace8c';

    fetch(`http://localhost:8080/api/watchLists/${userId}`)
        .then(response => {
            console.log('Watchlist API response status:', response.status);
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Failed to load watchlist');
            }
        })
        .then(watchlistItems => {
            console.log('Watchlist items loaded:', watchlistItems);
            displayWatchlistProperties(watchlistItems);
        })
        .catch(error => {
            console.error('Error loading watchlist:', error);
            displayEmptyWatchlist();
        });
}

function displayWatchlistProperties(watchlistItems) {
    console.log('displayWatchlistProperties called with:', watchlistItems);
    const propertiesList = document.getElementById('properties-list');
    console.log('Properties list element in display function:', propertiesList);

    if (!propertiesList) {
        console.error('Properties list container not found');
        return;
    }

    propertiesList.innerHTML = '';
    console.log('Cleared properties list innerHTML');

    if (watchlistItems.length === 0) {
        console.log('No watchlist items, showing empty message');
        propertiesList.innerHTML = `
            <div style="text-align: center; padding: 30px; color: #666;">
                <p>Brak zapisanych ogłoszeń</p>
                <p style="font-size: 14px;">Dodaj nieruchomości do watchlist, aby je tutaj zobaczyć</p>
            </div>
        `;
        return;
    }

    console.log('Processing', watchlistItems.length, 'watchlist items');
    watchlistItems.forEach((item, index) => {
        console.log(`Processing item ${index}:`, item);
        fetchOfferDetails(item.offerId).then(offerDetails => {
            console.log(`Offer details for item ${index}:`, offerDetails);
            if (offerDetails) {
                addPropertyToList(offerDetails, item.addedAt, item.offerId);
            }
        });
    });
}

function fetchOfferDetails(offerId) {
    const encodedOfferId = encodeURIComponent(offerId);
    return fetch(`http://localhost:8080/api/offer/show?offerId=${encodedOfferId}`)
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Failed to load offer details');
            }
        })
        .catch(error => {
            console.error('Error loading offer details for:', offerId, error);
            return null;
        });
}

function addPropertyToList(offerDetails, addedAt, offerId) {
    console.log('addPropertyToList called with:', offerDetails, addedAt, offerId);
    const propertiesList = document.getElementById('properties-list');
    console.log('Properties list element in addPropertyToList:', propertiesList);
    if (!propertiesList) {
        console.error('Properties list not found in addPropertyToList');
        return;
    }

    if (propertiesList.innerHTML.includes('Brak zapisanych ogłoszeń')) {
        propertiesList.innerHTML = '';
        console.log('Cleared empty message');
    }

    const propertyElement = document.createElement('div');
    propertyElement.className = 'property-item';

    const imageUrl = offerDetails.image_url || 'assets/no-photo.jpg';
    const address = offerDetails.city + ", " + offerDetails.street || 'Brak adresu';
    const area = offerDetails.size_m2 || 'N/A';
    const rooms = offerDetails.rooms || 'N/A';
    const floor = offerDetails.floor || 'N/A';
    const price = offerDetails.price_pln || 'Brak ceny';

    console.log('Creating property element with data:', { imageUrl, address, area, rooms, floor, price });

    propertyElement.innerHTML = `
        <div class="property-container" onclick="openOfferLink('${offerId}')" style="cursor: pointer;">
            <img src="${imageUrl}" alt="Mieszkanie" class="property-image" 
                 onerror="this.src='https://via.placeholder.com/300x200?text=Brak+zdjęcia'">
            <div class="property-details">
                <h4 class="property-address">${address}</h4>
                <p class="property-info">${area} • ${rooms} pokoje • ${floor}</p>
                <p class="property-price">${price} PLN</p>
                <p style="font-size: 12px; color: #666; margin-top: 5px;">
                    Dodano: ${new Date(addedAt).toLocaleDateString('pl-PL')}
                </p>
            </div>
            <button class="remove-property-btn" onclick="event.stopPropagation(); removeFromWatchlist('${offerId}')" title="Usuń z obserwowanych">
                <span class="material-icons">delete</span>
            </button>
        </div>
    `;

    propertiesList.appendChild(propertyElement);
    console.log('Property element appended to list. Total children:', propertiesList.children.length);
}

function displayEmptyWatchlist() {
    const propertiesList = document.getElementById('properties-list');
    if (!propertiesList) return;

    propertiesList.innerHTML = `
        <div style="text-align: center; padding: 30px; color: #666;">
            <p>Wystąpił błąd podczas ładowania zapisanych ogłoszeń</p>
            <p style="font-size: 14px;">Spróbuj odświeżyć stronę</p>
        </div>
    `;
}

function removeFromWatchlist(offerId) {
    const userId = 'ba6e97db-1f6f-41fc-a80a-5f948f0ace8c';

    if (!userId) {
        console.log('Brak userId');
        return;
    }

    if (confirm('Czy na pewno chcesz usunąć to ogłoszenie z obserwowanych?')) {
        const url = `http://localhost:8080/api/watchLists/remove?userId=${encodeURIComponent(userId)}&offerId=${encodeURIComponent(offerId)}`;

        fetch(url, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(async response => {
                const data = await response.json().catch(() => ({}));
                if (response.ok) {
                    console.log('Offer removed from watchlist');
                    const propertiesList = document.getElementById('properties-list');
                    if (propertiesList) {
                        propertiesList.innerHTML = '';
                        loadWatchlistProperties();
                    }
                } else {
                    throw new Error(data.message || 'Failed to remove offer from watchlist');
                }
            })
            .catch(error => {
                console.error('Error removing from watchlist:', error);
                alert(`Błąd: ${error.message}`);
            });
    }
}

function openOfferLink(offerId) {
    if (offerId && offerId.startsWith('http')) {
        window.open(offerId, '_blank');
    } else {
        console.error('Invalid offer URL:', offerId);
        alert('Nie można otworzyć linku do oferty');
    }
}

function setupAccountForm() {
    console.log('Setting up account form');
    console.log('Elements check:');
    console.log('properties-list element:', document.getElementById('properties-list'));
    console.log('username element:', document.querySelector('.username'));
    console.log('avatar-letter element:', document.querySelector('.avatar-letter'));

    initializeAccountComponent();
}

if (typeof window !== 'undefined') {
    window.loadUserData = loadUserData;
    window.loadWatchlistProperties = loadWatchlistProperties;
    window.removeFromWatchlist = removeFromWatchlist;
    window.openOfferLink = openOfferLink;
    window.setupAccountForm = setupAccountForm;
    window.initializeAccountComponent = initializeAccountComponent;
}
