const { test, expect } = require('@playwright/test');
const { HomePage } = require('../pages/HomePage');
const { RealEstateMapPage } = require('../pages/RealEstateMapPage');

test.describe('Map Navigation Tests', () => {
    let homePage;
    let mapPage;

    test.beforeEach(async ({ page }) => {
        homePage = new HomePage(page);
        mapPage = new RealEstateMapPage(page);
        await homePage.goto();
    });

    test('should navigate to map from home page', async ({ page }) => {
        await expect(homePage.heroSection).toBeVisible();

        await homePage.clickMapLink();

        const isMapVisible = await homePage.isMapContainerVisible();
        expect(isMapVisible).toBe(true);

        const isHomeVisible = await homePage.isHomeContentVisible();
        expect(isHomeVisible).toBe(false);
    });

    test('should load map after navigation from home page', async ({ page }) => {
        await homePage.clickMapLink();

        await mapPage.waitForMapLoad();

        const isMapVisible = await mapPage.isMapVisible();
        expect(isMapVisible).toBe(true);
    });

    test('should check if map is interactive', async ({ page }) => {
        await homePage.clickMapLink();

        await mapPage.waitForMapLoad();

        const isInteractive = await mapPage.isMapInteractive();
        expect(isInteractive).toBe(true);
    });

});
