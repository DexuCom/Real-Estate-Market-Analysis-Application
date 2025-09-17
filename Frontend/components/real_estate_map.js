
var map = null;
var tileLayers = {};
var currentLayer = null;
var heatmapLayer = null;
var heatmapData = [];
var markerClusterGroup = null;

function initializeRealEstateMap() {
    console.log('Initializing Real Estate Map');

    map = L.map('map', {
        maxZoom: 18,
        minZoom: 8
    }).setView([54.37, 18.63], 11);

    if (typeof L.markerClusterGroup === 'function') {
        markerClusterGroup = L.markerClusterGroup({
            maxClusterRadius: 60,
            spiderfyOnMaxZoom: true,
            showCoverageOnHover: false,
            zoomToBoundsOnClick: true,
            iconCreateFunction: function (cluster) {
                const childCount = cluster.getChildCount();
                let className = 'marker-cluster-';

                if (childCount < 10) {
                    className += 'small';
                } else if (childCount < 100) {
                    className += 'medium';
                } else {
                    className += 'large';
                }

                return new L.DivIcon({
                    html: '<div><span>' + childCount + '</span></div>',
                    className: 'marker-cluster ' + className,
                    iconSize: new L.Point(40, 40)
                });
            }
        });

        map.addLayer(markerClusterGroup);
        console.log('Marker clustering enabled');
    } else {
        console.warn('Leaflet.markercluster plugin not available - clustering disabled');
        markerClusterGroup = null;
    }

    tileLayers = {
        'openstreetmap': L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap',
            maxZoom: 19
        }),
        'cartodb': L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
            attribution: '© OpenStreetMap © CartoDB',
            maxZoom: 19
        }),
        'cartodb-dark': L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
            attribution: '© OpenStreetMap © CartoDB',
            maxZoom: 19
        }),
        'stamen-terrain': L.tileLayer('https://stamen-tiles-{s}.a.ssl.fastly.net/terrain/{z}/{x}/{y}{r}.png', {
            attribution: 'Map tiles by Stamen Design, CC BY 3.0 — Map data © OpenStreetMap',
            maxZoom: 18
        }),
        'stamen-toner': L.tileLayer('https://stamen-tiles-{s}.a.ssl.fastly.net/toner/{z}/{x}/{y}{r}.png', {
            attribution: 'Map tiles by Stamen Design, CC BY 3.0 — Map data © OpenStreetMap',
            maxZoom: 18
        }),
        'esri-satellite': L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
            attribution: 'Tiles © Esri — Source: Esri, Maxar, GeoEye, Earthstar Geographics, CNES/Airbus DS, USDA, USGS, AeroGRID, IGN, and the GIS User Community',
            maxZoom: 18
        })
    };

    currentLayer = tileLayers['openstreetmap'];
    currentLayer.addTo(map);

    setupEventListeners();

    loadPropertyData();
}

function setupEventListeners() {
    const styleSelect = document.getElementById('styleSelect');
    if (styleSelect) {
        styleSelect.addEventListener('change', function (e) {
            map.removeLayer(currentLayer);
            currentLayer = tileLayers[e.target.value];
            currentLayer.addTo(map);
        });
    }

    const heatmapToggle = document.getElementById('heatmapToggle');
    if (heatmapToggle) {
        heatmapToggle.addEventListener('change', toggleHeatmap);
    }

    map.on('moveend zoomend', function () {
        if (heatmapLayer && document.getElementById('heatmapToggle').checked) {
            map.removeLayer(heatmapLayer);
            heatmapLayer = null;
            toggleHeatmap();
        }
    });

    const closePanel = document.getElementById('closePanel');
    if (closePanel) {
        closePanel.addEventListener('click', function () {
            const panel = document.getElementById('propertyPanel');
            panel.classList.remove('active');
        });
    }

    const filterButton = document.getElementById('filterButton');
    if (filterButton) {
        filterButton.addEventListener('click', function () {
            const button = this;
            const span = button.querySelector('span');
            const icon = button.querySelector('.material-icons');
            const filterPanel = document.getElementById('filterPanel');

            if (span.textContent === 'Schowaj filtry') {
                span.textContent = 'Pokaż filtry';
                icon.textContent = 'keyboard_arrow_down';
                filterPanel.classList.remove('active');
            } else {
                span.textContent = 'Schowaj filtry';
                icon.textContent = 'keyboard_arrow_up';
                filterPanel.classList.add('active');
            }
        });
    }
}

function calculatePricePerSqm(price, size) {
    const priceNum = parseFloat(price.replace(/[^\d]/g, ''));
    const sizeNum = parseFloat(size.replace(/[^\d]/g, ''));
    return sizeNum > 0 ? priceNum / sizeNum : 0;
}

function createBackgroundHeatmapData() {
    const backgroundPoints = [];
    const bounds = map.getBounds();
    const latRange = bounds.getNorth() - bounds.getSouth();
    const lngRange = bounds.getEast() - bounds.getWest();

    const gridSize = 25;
    const latStep = latRange / gridSize;
    const lngStep = lngRange / gridSize;

    for (let i = 0; i <= gridSize; i++) {
        for (let j = 0; j <= gridSize; j++) {
            const lat = bounds.getSouth() + (i * latStep);
            const lng = bounds.getWest() + (j * lngStep);

            backgroundPoints.push([lat, lng, 0.01]);
        }
    }
    return backgroundPoints;
}

function toggleHeatmap() {
    const isChecked = document.getElementById('heatmapToggle').checked;

    if (isChecked) {
        if (!heatmapLayer) {
            const backgroundData = createBackgroundHeatmapData();
            const combinedData = [...backgroundData, ...heatmapData];

            heatmapLayer = L.heatLayer(combinedData, {
                radius: 70,
                blur: 50,
                maxZoom: 15,
                minOpacity: 0.15,
                gradient: {
                    0.0: '#4CAF50',
                    0.1: '#4CAF50',
                    0.3: '#66BB6A',
                    0.5: '#FFC107',
                    0.7: '#FF9800',
                    1.0: '#F44336'
                }
            }).addTo(map);
        }
    } else {
        if (heatmapLayer) {
            map.removeLayer(heatmapLayer);
            heatmapLayer = null;
        }
    }
}

function loadPropertyData() {
    console.log('Loading property data from API...');

    if (markerClusterGroup) {
        markerClusterGroup.clearLayers();
    }

    heatmapData = [];

    fetch('http://localhost:8080/api/offer/map-points')
        .then(response => {
            console.log('Fetch response status:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Property data loaded from API, count:', data.length);
            console.log('Sample data:', data.slice(0, 3));

            data.forEach(function (offer, index) {
                console.log(`Offer ${index}:`, {
                    id: offer.id,
                    latitude: offer.latitude,
                    longitude: offer.longitude,
                    pricePln: offer.pricePln
                });

                if (offer.latitude && offer.longitude) {
                    const lat = parseFloat(offer.latitude);
                    const lng = parseFloat(offer.longitude);

                    console.log(`Parsed coordinates for offer ${offer.id}: lat=${lat}, lng=${lng}`);

                    if (!isNaN(lat) && !isNaN(lng)) {
                        createPropertyMarker(offer, lat, lng);
                    } else {
                        console.error(`Invalid coordinates for offer ${offer.id}:`, offer.latitude, offer.longitude);
                    }
                } else {
                    console.warn(`Missing coordinates for offer ${index}:`, offer);
                }
            });
        })
        .catch(error => {
            console.error('Error loading property data:', error);
        });
}

function createPropertyMarker(offer, lat, lng) {
    heatmapData.push([lat, lng, 0.5]);

    const homeIcon = L.divIcon({
        className: 'custom-home-icon',
        html: '<span class="material-icons">home</span>',
        iconSize: [30, 30],
        iconAnchor: [15, 15],
        popupAnchor: [0, -15]
    });

    const marker = L.marker([lat, lng], { icon: homeIcon });

    if (markerClusterGroup) {
        markerClusterGroup.addLayer(marker);
    } else {
        marker.addTo(map);
    }

    const estimatedSize = 50;
    const pricePerSqm = offer.pricePln / estimatedSize;

    marker.on('click', function () {
        showPropertyPanel(offer, pricePerSqm);
    });

    setTimeout(() => {
        const iconElement = marker.getElement();
        if (iconElement) {
            iconElement.style.setProperty('background-color', '#8b5cf6', 'important');
        }
    }, 200);
}

async function showPropertyPanel(offer, pricePerSqm) {
    const panel = document.getElementById('propertyPanel');

    const addButton = document.getElementById('addToWatchlist');
    addButton.textContent = 'Dodaj do obserwowanych';
    addButton.classList.remove('added');
    addButton.disabled = false;

    setLoadingState();

    panel.classList.add('active');

    try {
        const response = await fetch(`http://localhost:8080/api/offer/show?offerId=${encodeURIComponent(offer.id)}`);
        if (response.ok) {
            const offerDetails = await response.json();
            populatePropertyDetails(offerDetails, offer, pricePerSqm);
        } else {
            setErrorState('Błąd pobierania danych z API');
        }
    } catch (error) {
        console.error('Błąd podczas pobierania szczegółów oferty:', error);
        setErrorState('Błąd połączenia z API');
    }
}

function setLoadingState() {
    const loadingText = 'Ładowanie...';

    document.getElementById('propertyStreet').textContent = loadingText;
    document.getElementById('currentPrice').textContent = loadingText;
    document.getElementById('propertySize').textContent = loadingText;
    document.getElementById('propertyRooms').textContent = loadingText;
    document.getElementById('propertyFloor').textContent = loadingText;
    document.getElementById('propertyYear').textContent = loadingText;
    document.getElementById('propertyMarket').textContent = loadingText;
    document.getElementById('propertyHeating').textContent = loadingText;
    document.getElementById('propertyTotalFloors').textContent = loadingText;
    document.getElementById('pricePerSqm').textContent = loadingText;
    document.getElementById('propertyAmenities').innerHTML = `<span>${loadingText}</span>`;

    const imageDiv = document.getElementById('propertyImage');
    imageDiv.innerHTML = `<div style="height: 200px; background: #f0f0f0; display: flex; align-items: center; justify-content: center; color: #666; font-family: 'Outfit', Arial, sans-serif;">${loadingText}</div>`;
}

function setErrorState(errorMessage) {
    document.getElementById('propertyStreet').textContent = errorMessage;
    document.getElementById('currentPrice').textContent = 'Błąd połączenia';
    document.getElementById('propertySize').textContent = 'Błąd połączenia';
    document.getElementById('propertyRooms').textContent = 'Błąd połączenia';
    document.getElementById('propertyFloor').textContent = 'Błąd połączenia';
    document.getElementById('propertyYear').textContent = 'Błąd połączenia';
    document.getElementById('propertyMarket').textContent = 'Błąd połączenia';
    document.getElementById('propertyHeating').textContent = 'Błąd połączenia';
    document.getElementById('propertyTotalFloors').textContent = 'Błąd połączenia';
    document.getElementById('pricePerSqm').textContent = 'Błąd połączenia';
    document.getElementById('propertyAmenities').innerHTML = '<span>Błąd połączenia z API</span>';

    const imageDiv = document.getElementById('propertyImage');
    imageDiv.innerHTML = '<div style="height: 200px; background: #f0f0f0; display: flex; align-items: center; justify-content: center; color: #666; font-family: \'Outfit\', Arial, sans-serif;">Błąd połączenia</div>';

    document.getElementById('viewOffer').onclick = null;
    document.getElementById('addToWatchlist').onclick = null;
}

function populatePropertyDetails(offerDetails, offer, pricePerSqm) {
    document.getElementById('propertyStreet').textContent = offerDetails.street || 'Brak danych';
    document.getElementById('currentPrice').textContent = (offerDetails.pricePln ? offerDetails.pricePln.toLocaleString() + ' PLN' : 'Brak danych');
    document.getElementById('propertySize').textContent = offerDetails.sizeM2 || 'Brak danych';
    document.getElementById('propertyRooms').textContent = offerDetails.rooms || 'Brak danych';
    document.getElementById('propertyFloor').textContent = offerDetails.floor || 'Brak danych';
    document.getElementById('propertyYear').textContent = offerDetails.yearBuilt || 'Brak danych';
    document.getElementById('propertyMarket').textContent = offerDetails.market ? `Rynek ${offerDetails.market}` : 'Brak danych';
    document.getElementById('propertyHeating').textContent = offerDetails.heating || 'Brak danych';
    document.getElementById('propertyTotalFloors').textContent = offerDetails.totalFloors ? `Budynek ${offerDetails.totalFloors}-piętrowy` : 'Brak danych';

    let apiPricePerSqm = pricePerSqm;
    if (offerDetails.pricePln && offerDetails.sizeM2) {
        const sizeNum = parseFloat(offerDetails.sizeM2.toString().replace(/[^\d]/g, ''));
        if (sizeNum > 0) {
            apiPricePerSqm = offerDetails.pricePln / sizeNum;
        }
    }
    document.getElementById('pricePerSqm').textContent = Math.round(apiPricePerSqm).toLocaleString() + ' zł / m²';

    setPropertyAmenities(offerDetails);

    setPropertyImage(offerDetails);

    document.getElementById('viewOffer').onclick = function () {
        window.open(offerDetails.detailUrl, '_blank');
    };

    document.getElementById('addToWatchlist').onclick = function () {
        addToWatchlist(offer.id);
    };
}

function setPropertyAmenities(offerDetails) {
    const amenitiesContainer = document.getElementById('propertyAmenities');
    const amenities = [];

    if (offerDetails.intercom === 1) amenities.push('🔔 Domofon');
    if (offerDetails.basement === 1) amenities.push('🏠 Piwnica');
    if (offerDetails.furnished === 1) amenities.push('🪑 Umeblowane');
    if (offerDetails.elevator === 1) amenities.push('🛗 Winda');
    if (offerDetails.parkingSpace === 1) amenities.push('🚗 Miejsce parkingowe');
    if (offerDetails.gatedProperty === 1) amenities.push('🚪 Osiedle zamknięte');
    if (offerDetails.balcony === 1) amenities.push('🌅 Balkon');
    if (offerDetails.terrace === 1) amenities.push('🏞️ Taras');
    if (offerDetails.garden === 1) amenities.push('🌳 Ogród');

    if (amenities.length > 0) {
        amenitiesContainer.innerHTML = amenities.map(amenity => `<span class="amenity-item">${amenity}</span>`).join('');
    } else {
        amenitiesContainer.innerHTML = '<span>Brak dodatkowych udogodnień</span>';
    }
}

function setPropertyImage(offerDetails) {
    const imageDiv = document.getElementById('propertyImage');

    if (offerDetails.imageUrl) {
        imageDiv.innerHTML = `<img src="${offerDetails.imageUrl}" alt="Zdjęcie mieszkania">`;
    } else {
        imageDiv.innerHTML = '<div style="height: 200px; background: #f0f0f0; display: flex; align-items: center; justify-content: center; color: #666; font-family: \'Outfit\', Arial, sans-serif;">Brak zdjęcia</div>';
    }
}

function addToWatchlist(offerId) {
    console.log('Adding to watchlist:', offerId);

    const authToken = localStorage.getItem('authToken');
    if (!authToken) {
        alert('Zaloguj się aby dodać ofertę do obserwowanych');
        return;
    }

    const userId = parent.getCurrentUserId();
    if (!userId) {
        alert('Błąd: Brak informacji o użytkowniku. Zaloguj się ponownie.');
        return;
    }

    const addButton = document.getElementById('addToWatchlist');
    const originalText = addButton.textContent;

    addButton.disabled = true;
    addButton.textContent = 'Dodawanie...';

    const url = `http://localhost:8080/api/watchLists/add?userId=${encodeURIComponent(userId)}&offerId=${encodeURIComponent(offerId)}`;

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(async response => {
            if (response.ok) {
                addButton.textContent = 'Dodano ✓';
                addButton.classList.add('added');
                addButton.disabled = false;
                console.log('Successfully added to watchlist');
            } else {
                const data = await response.json();
                throw new Error(data.message || 'Nieznany błąd');
            }
        })
        .catch(error => {
            console.error('Error adding to watchlist:', error);
            alert(`Błąd: ${error.message}`);
            addButton.textContent = originalText;
            addButton.disabled = false;
        });
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeRealEstateMap);
} else {
    initializeRealEstateMap();
}

if (typeof window !== 'undefined') {
    window.initializeRealEstateMap = initializeRealEstateMap;
    window.toggleHeatmap = toggleHeatmap;
    window.showPropertyPanel = showPropertyPanel;
    window.addToWatchlist = addToWatchlist;
}
