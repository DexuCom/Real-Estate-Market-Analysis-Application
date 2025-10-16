let currentOfferData = null;
let currentOfferDetails = null;

function initializeScoringReport() {
    console.log('Initializing Scoring Report');

    function checkDOMReady() {
        const testElement = document.getElementById('reportStreet');
        if (!testElement) {
            console.log('DOM not ready yet, waiting...');
            setTimeout(checkDOMReady, 50);
            return;
        }

        loadReportData();
    }

    function loadReportData() {
        const reportDataStr = localStorage.getItem('currentReportData');
        console.log('Report data from localStorage:', reportDataStr);

        if (reportDataStr) {
            try {
                const data = JSON.parse(reportDataStr);
                currentOfferData = data.offer;
                currentOfferDetails = data.offerDetails;

                console.log('Report data parsed:', data);
                console.log('Offer details:', currentOfferDetails);
                console.log('Offer:', currentOfferData);

                populateReport(currentOfferDetails, currentOfferData, data.pricePerSqm);
            } catch (error) {
                console.error('Error parsing report data:', error);
                showError('Błąd wczytywania danych oferty');
            }
        } else {
            console.error('No report data found in localStorage');
            showError('Brak danych oferty');
        }

        setupEventListeners();
    }

    checkDOMReady();
}

function setupEventListeners() {
    const closeButton = document.getElementById('closeReport');
    if (closeButton) {
        closeButton.addEventListener('click', closeReport);
    }

    const viewOfferButton = document.getElementById('viewOriginalOffer');
    if (viewOfferButton) {
        viewOfferButton.addEventListener('click', openOriginalOffer);
    }

    const addToWatchlistButton = document.getElementById('addToWatchlistFromReport');
    if (addToWatchlistButton) {
        addToWatchlistButton.addEventListener('click', addToWatchlistFromReport);
    }
}

function populateReport(offerDetails, offer, pricePerSqm) {
    console.log('populateReport called with:', { offerDetails, offer, pricePerSqm });

    if (!offerDetails) {
        console.error('No offer details provided');
        return;
    }

    const requiredElements = [
        'reportStreet', 'reportPrice', 'reportPricePerSqm', 'reportSize',
        'reportRooms', 'reportFloor', 'reportTotalFloors', 'reportYear',
        'reportMarket', 'reportHeating'
    ];

    for (const elementId of requiredElements) {
        if (!document.getElementById(elementId)) {
            console.error(`Element ${elementId} not found in DOM`);
            return;
        }
    }

    document.getElementById('reportStreet').textContent = offerDetails.street || 'Brak danych';

    document.getElementById('reportPrice').textContent = offerDetails.pricePln ?
        offerDetails.pricePln.toLocaleString() + ' PLN' : 'Brak danych';

    let pricePerSqmText = 'Brak danych';
    if (pricePerSqm) {
        pricePerSqmText = Math.round(pricePerSqm).toLocaleString() + ' zł / m²';
    } else if (offer && offer.pm2) {
        pricePerSqmText = Math.round(offer.pm2).toLocaleString() + ' zł / m²';
    }
    document.getElementById('reportPricePerSqm').textContent = pricePerSqmText;

    document.getElementById('reportSize').textContent = offerDetails.sizeM2 ?
        offerDetails.sizeM2 + ' m²' : 'Brak danych';

    document.getElementById('reportRooms').textContent = offerDetails.rooms || 'Brak danych';

    document.getElementById('reportFloor').textContent = offerDetails.floor ?
        'Piętro ' + offerDetails.floor : 'Brak danych';

    document.getElementById('reportTotalFloors').textContent = offerDetails.totalFloors ?
        `Budynek ${offerDetails.totalFloors}-piętrowy` : 'Brak danych';

    const yearElement = document.getElementById('reportYear');
    const yearContainer = yearElement.closest('.info-item');
    if (offerDetails.yearBuilt && offerDetails.yearBuilt !== -1) {
        yearElement.textContent = offerDetails.yearBuilt + ' r.';
        if (yearContainer) yearContainer.style.display = 'flex';
    } else {
        if (yearContainer) yearContainer.style.display = 'none';
    }

    document.getElementById('reportMarket').textContent = offerDetails.market ?
        `Rynek ${offerDetails.market}` : 'Brak danych';

    const heatingElement = document.getElementById('reportHeating');
    const heatingContainer = heatingElement.closest('.info-item');
    if (offerDetails.heating && offerDetails.heating !== -1) {
        heatingElement.textContent = offerDetails.heating;
        if (heatingContainer) heatingContainer.style.display = 'flex';
    } else {
        if (heatingContainer) heatingContainer.style.display = 'none';
    }

    setReportImage(offerDetails);

    setReportAmenities(offerDetails);

    calculatePredictedPriceAndScore(offerDetails);
}

function calculatePredictedPriceAndScore(offerDetails) {
    const actualPrice = offerDetails.pricePln;

    if (!actualPrice) {
        document.getElementById('predictedPrice').textContent = 'Brak danych';
        document.getElementById('gaugeScore').textContent = '--';
        document.getElementById('scoreLabel').textContent = 'Brak danych';
        return;
    }

    const deviation = (Math.random() * 60) - 30;
    const predictedPrice = Math.round(actualPrice * (1 + deviation / 100));

    const percentageDifference = Math.abs(deviation);

    let score;
    if (percentageDifference <= 5) {
        score = 100 - (percentageDifference * 2);
    } else if (percentageDifference <= 15) {
        score = 90 - ((percentageDifference - 5) * 3);
    } else if (percentageDifference <= 25) {
        score = 60 - ((percentageDifference - 15) * 2);
    } else {
        score = 40 - ((percentageDifference - 25) * 8);
    }

    score = Math.max(0, Math.min(100, Math.round(score)));

    document.getElementById('predictedPrice').textContent = predictedPrice.toLocaleString() + ' PLN';

    drawGauge(score, actualPrice, predictedPrice);
}

function setReportImage(offerDetails) {
    const imageDiv = document.getElementById('reportImage');

    if (offerDetails.imageUrl) {
        imageDiv.innerHTML = `<img src="${offerDetails.imageUrl}" alt="Zdjęcie nieruchomości">`;
    } else {
        imageDiv.innerHTML = '<div style="height: 100%; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); display: flex; align-items: center; justify-content: center; color: #666; font-size: 18px;">Brak zdjęcia</div>';
    }
}

function setReportAmenities(offerDetails) {
    const amenitiesContainer = document.getElementById('reportAmenities');
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
        amenitiesContainer.innerHTML = amenities.map(amenity =>
            `<div class="amenity-item">${amenity}</div>`
        ).join('');
    } else {
        amenitiesContainer.innerHTML = '<div class="amenity-item">Brak dodatkowych udogodnień</div>';
    }
}

function drawGauge(score, actualPrice, predictedPrice) {
    const canvas = document.getElementById('gaugeCanvas');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const centerX = canvas.width / 2;
    const centerY = canvas.height - 12;
    const radius = 100;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    ctx.beginPath();
    ctx.arc(centerX, centerY, radius, Math.PI, 2 * Math.PI, false);
    ctx.lineWidth = 20;
    ctx.strokeStyle = '#e0e0e0';
    ctx.stroke();

    const startAngle = Math.PI;
    const endAngle = Math.PI + (Math.PI * (score / 100));

    let color;
    if (score < 30) {
        color = '#f44336';
    } else if (score < 50) {
        color = '#ff9800';
    } else if (score < 70) {
        color = '#ffeb3b';
    } else if (score < 85) {
        color = '#8bc34a';
    } else {
        color = '#4caf50';
    }

    ctx.beginPath();
    ctx.arc(centerX, centerY, radius, startAngle, endAngle, false);
    ctx.lineWidth = 20;
    ctx.strokeStyle = color;
    ctx.lineCap = 'round';
    ctx.stroke();

    for (let i = 0; i <= 100; i += 20) {
        const angle = Math.PI + (Math.PI * (i / 100));
        const x1 = centerX + (radius - 12) * Math.cos(angle);
        const y1 = centerY + (radius - 12) * Math.sin(angle);
        const x2 = centerX + (radius + 7) * Math.cos(angle);
        const y2 = centerY + (radius + 7) * Math.sin(angle);

        ctx.beginPath();
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.lineWidth = 2;
        ctx.strokeStyle = '#999';
        ctx.stroke();

        const textX = centerX + (radius + 22) * Math.cos(angle);
        const textY = centerY + (radius + 22) * Math.sin(angle);
        ctx.fillStyle = '#666';
        ctx.font = '10px Outfit';
        ctx.textAlign = 'center';
        ctx.fillText(i.toString(), textX, textY + 5);
    }

    document.getElementById('gaugeScore').textContent = score;

    let label = '';
    if (score < 30) {
        label = 'Słaba wycena';
    } else if (score < 50) {
        label = 'Przeciętna wycena';
    } else if (score < 70) {
        label = 'Dobra wycena';
    } else if (score < 85) {
        label = 'Bardzo dobra wycena';
    } else {
        label = 'Doskonała wycena';
    }
    document.getElementById('scoreLabel').textContent = label;
}

function closeReport() {
    if (window.parent && window.parent.loadComponent) {
        window.parent.loadComponent('real_estate_map');
    }
}

function openOriginalOffer() {
    if (currentOfferDetails && currentOfferDetails.detailUrl) {
        window.open(currentOfferDetails.detailUrl, '_blank');
    } else {
        alert('Brak adresu URL oryginalnej oferty');
    }
}

function addToWatchlistFromReport() {
    if (!currentOfferData || !currentOfferData.id) {
        alert('Błąd: Brak informacji o ofercie');
        return;
    }

    const authToken = localStorage.getItem('authToken');
    if (!authToken) {
        alert('Zaloguj się aby dodać ofertę do obserwowanych');
        return;
    }

    const userId = window.parent ? window.parent.getCurrentUserId() : null;
    if (!userId) {
        alert('Błąd: Brak informacji o użytkowniku. Zaloguj się ponownie.');
        return;
    }

    const addButton = document.getElementById('addToWatchlistFromReport');
    const originalHTML = addButton.innerHTML;

    addButton.disabled = true;
    addButton.innerHTML = '<span class="material-icons">hourglass_empty</span>Dodawanie...';

    const url = `http://localhost:8080/api/watchLists/add?userId=${encodeURIComponent(userId)}&offerId=${encodeURIComponent(currentOfferData.id)}`;

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(async response => {
            if (response.ok) {
                addButton.innerHTML = '<span class="material-icons">check</span>Dodano ✓';
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
            addButton.innerHTML = originalHTML;
            addButton.disabled = false;
        });
}

function showError(message) {
    const reportContent = document.querySelector('.report-content');
    if (reportContent) {
        reportContent.innerHTML = `
            <div style="text-align: center; padding: 60px 20px;">
                <span class="material-icons" style="font-size: 64px; color: #f44336;">error_outline</span>
                <h2 style="margin-top: 20px; color: #333;">${message}</h2>
                <button onclick="closeReport()" class="btn btn-primary" style="margin-top: 30px;">
                    Zamknij
                </button>
            </div>
        `;
    }
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeScoringReport);
} else {
    initializeScoringReport();
}

function reinitializeReport() {
    console.log('Reinitializing report with new data');
    initializeScoringReport();
}

if (typeof window !== 'undefined') {
    window.initializeScoringReport = initializeScoringReport;
    window.reinitializeReport = reinitializeReport;
    window.closeReport = closeReport;
}
