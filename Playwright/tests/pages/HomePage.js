class HomePage {
    constructor(page) {
        this.page = page;
        this.header = page.locator('.header');
        this.logo = page.locator('.logo');
        this.homeLink = page.locator('a[onclick*="showHome"]');
        this.mapLink = page.locator('a[onclick*="showMap"]');
        this.accountLink = page.locator('a[onclick*="showAccount"]');
        this.loginButton = page.locator('#login-btn');
        this.registerButton = page.locator('#register-btn');

        this.heroSection = page.locator('.hero-section');
        this.heroTitle = page.locator('.hero-title');
        this.heroDescription = page.locator('.hero-desc');
        this.heroImage = page.locator('#hero-image');

        this.heatmapTabButton = page.locator('button.hero-tab-btn').filter({ hasText: 'Mapa ciepła' });
        this.searchTabButton = page.locator('button.hero-tab-btn').filter({ hasText: 'Konto użytkownika' });
        this.aiTabButton = page.locator('button.hero-tab-btn').filter({ hasText: 'Model scoringowy AI' });

        this.homePlaceholder = page.locator('#home-placeholder');
        this.mapContainer = page.locator('#mapContainer');
    }

    async goto() {
        await this.page.goto('http://127.0.0.1:5500/Frontend/index.html');
    }

    async isPageLoaded() {
        await this.header.waitFor({ state: 'visible' });
        await this.heroSection.waitFor({ state: 'visible' });
        return true;
    }

    async getPageTitle() {
        return await this.page.title();
    }

    async getHeroTitle() {
        return await this.heroTitle.textContent();
    }

    async isHeroImageVisible() {
        return await this.heroImage.isVisible();
    }

    async clickMapLink() {
        await this.mapLink.click();
    }

    async clickHeatmapTab() {
        await this.heatmapTabButton.click();
    }

    async clickSearchTab() {
        await this.searchTabButton.click();
    }

    async clickAiTab() {
        await this.aiTabButton.click();
    }

    async isHomeContentVisible() {
        return await this.homePlaceholder.isVisible();
    }

    async isMapContainerVisible() {
        return await this.mapContainer.isVisible();
    }

    async isNavigationVisible() {
        const homeVisible = await this.homeLink.isVisible();
        const mapVisible = await this.mapLink.isVisible();
        const accountVisible = await this.accountLink.isVisible();
        return homeVisible && mapVisible && accountVisible;
    }
}

module.exports = { HomePage };
