function switchTab(tab, button) {
    document.querySelectorAll('.hero-tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    button.classList.add('active');

    const heroImage = document.getElementById('hero-image');
    if (heroImage) {
        heroImage.classList.add('fade-out');

        setTimeout(() => {
            let newSrc, newAlt;

            switch (tab) {
                case 'heatmap':
                    newSrc = 'assets/heatmap.png';
                    newAlt = 'Mapa ciepła cen';
                    break;
                case 'search':
                    newSrc = 'assets/2.png';
                    newAlt = 'Wyszukiwarka nieruchomości';
                    break;
                case 'ai':
                    newSrc = 'assets/3.png';
                    newAlt = 'Model scoringowy AI';
                    break;
            }

            heroImage.src = newSrc;
            heroImage.alt = newAlt;

            setTimeout(() => {
                heroImage.classList.remove('fade-out');
                heroImage.classList.add('fade-in');

                setTimeout(() => {
                    heroImage.classList.remove('fade-in');
                }, 250);
            }, 30);
        }, 125);
    }
}

function initializeHomeComponent() {
    console.log('Home component initialized');

    preloadImages();
    setInitialImage();
}

function setInitialImage() {
    // Znajdź aktywny przycisk
    const activeButton = document.querySelector('.hero-tab-btn.active');
    const heroImage = document.getElementById('hero-image');

    if (activeButton && heroImage) {
        // Sprawdź który tab jest aktywny na podstawie onclick
        const onclick = activeButton.getAttribute('onclick');

        if (onclick.includes("'heatmap'")) {
            heroImage.src = 'assets/heatmap.png';
            heroImage.alt = 'Mapa ciepła cen';
        } else if (onclick.includes("'search'")) {
            heroImage.src = 'assets/2.png';
            heroImage.alt = 'Wyszukiwarka nieruchomości';
        } else if (onclick.includes("'ai'")) {
            heroImage.src = 'assets/3.png';
            heroImage.alt = 'Model scoringowy AI';
        }
    }
}

function preloadImages() {
    const imageSources = [
        'assets/heatmap.png',
        'assets/2.png',
        'assets/3.png'
    ];

    imageSources.forEach(src => {
        const img = new Image();
        img.src = src;
    });
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeHomeComponent);
} else {
    initializeHomeComponent();
}
