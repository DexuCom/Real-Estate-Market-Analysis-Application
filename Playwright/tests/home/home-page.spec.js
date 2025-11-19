const { test, expect } = require('@playwright/test');
const { HomePage } = require('../pages/HomePage');

test.describe('Home Page Tests', () => {
    let homePage;

    test.beforeEach(async ({ page }) => {
        homePage = new HomePage(page);
        await homePage.goto();
    });

    test('should load home page successfully', async ({ page }) => {
        await expect(homePage.isPageLoaded()).resolves.toBe(true);

        const title = await homePage.getPageTitle();
        expect(title).toBe('EstateScout');
    });

    test('should display header with logo', async () => {
        await expect(homePage.header).toBeVisible();

        await expect(homePage.logo).toBeVisible();
    });

    test('should display navigation menu', async () => {
        const isNavVisible = await homePage.isNavigationVisible();
        expect(isNavVisible).toBe(true);

        await expect(homePage.homeLink).toBeVisible();
        await expect(homePage.mapLink).toBeVisible();
        await expect(homePage.accountLink).toBeVisible();
    });

    test('should display hero section with title and description', async () => {
        await expect(homePage.heroSection).toBeVisible();

        await expect(homePage.heroTitle).toBeVisible();
        const heroTitle = await homePage.getHeroTitle();
        expect(heroTitle).toContain('Znajdź nieruchomość');

        await expect(homePage.heroDescription).toBeVisible();
    });

    test('should display hero image', async () => {
        const isImageVisible = await homePage.isHeroImageVisible();
        expect(isImageVisible).toBe(true);

        await expect(homePage.heroImage).toBeVisible();
    });

    test('should display all three tab buttons', async () => {
        await expect(homePage.heatmapTabButton).toBeVisible();
        await expect(homePage.searchTabButton).toBeVisible();
        await expect(homePage.aiTabButton).toBeVisible();
    });

    test('should have heatmap tab active by default', async () => {
        await expect(homePage.heatmapTabButton).toHaveClass(/active/);
    });

    test('should switch tabs when clicking on them', async () => {
        await homePage.clickSearchTab();
        await expect(homePage.searchTabButton).toHaveClass(/active/);

        await homePage.clickAiTab();
        await expect(homePage.aiTabButton).toHaveClass(/active/);

        await homePage.clickHeatmapTab();
        await expect(homePage.heatmapTabButton).toHaveClass(/active/);
    });

    test('should display login and register buttons when not logged in', async () => {
        await expect(homePage.loginButton).toBeVisible();
        await expect(homePage.registerButton).toBeVisible();
    });

    test('should have home content visible by default', async () => {
        const isHomeVisible = await homePage.isHomeContentVisible();
        expect(isHomeVisible).toBe(true);
    });

    test('should have map container hidden initially', async () => {
        const isMapVisible = await homePage.isMapContainerVisible();
        expect(isMapVisible).toBe(false);
    });
});
