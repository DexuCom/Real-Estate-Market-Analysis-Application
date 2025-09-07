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
    initParallax();
}

function initParallax() {
    const isMobile = window.innerWidth <= 768 || /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);

    if (isMobile) {
        return;
    }

    const parallaxElements = document.querySelectorAll('.parallax-element');
    const parallaxBg = document.querySelector('.parallax-bg');
    const parallaxOverlay = document.querySelector('.parallax-overlay');

    let mouseX = 0;
    let mouseY = 0;
    let targetX = 0;
    let targetY = 0;

    document.addEventListener('mousemove', (e) => {
        targetX = (e.clientX / window.innerWidth) - 0.5;
        targetY = (e.clientY / window.innerHeight) - 0.5;
    });

    function animate() {
        mouseX += (targetX - mouseX) * 0.1;
        mouseY += (targetY - mouseY) * 0.1;

        if (parallaxBg) {
            const bgMoveX = mouseX * 20;
            const bgMoveY = mouseY * 20;
            parallaxBg.style.transform = `translate(${bgMoveX}px, ${bgMoveY}px)`;
        }

        if (parallaxOverlay) {
            const overlayMoveX = mouseX * -15;
            const overlayMoveY = mouseY * -15;
            parallaxOverlay.style.transform = `translate(${overlayMoveX}px, ${overlayMoveY}px)`;
        }

        parallaxElements.forEach(element => {
            const speed = parseFloat(element.dataset.speed) || 0.5;
            const moveX = mouseX * speed * 30;
            const moveY = mouseY * speed * 30;

            element.style.transform = `translate(${moveX}px, ${moveY}px)`;
        });

        requestAnimationFrame(animate);
    }

    animate();
}

function setInitialImage() {
    const activeButton = document.querySelector('.hero-tab-btn.active');
    const heroImage = document.getElementById('hero-image');

    if (activeButton && heroImage) {
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
