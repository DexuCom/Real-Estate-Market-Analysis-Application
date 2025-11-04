class RealEstateMapPage {
    constructor(page) {
        this.page = page;
        this.mapFrame = null;

        this.mapContainer = page.locator('#mapContainer');
        this.mapIframe = page.locator('#mapFrame');
    }

    async waitForMapLoad() {
        await this.mapContainer.waitFor({ state: 'visible', timeout: 10000 });
        await this.mapIframe.waitFor({ state: 'visible', timeout: 10000 });

        this.mapFrame = this.page.frameLocator('#mapFrame');

        await this.mapFrame.locator('#map').waitFor({ state: 'visible', timeout: 10000 });
        await this.mapFrame.locator('.leaflet-container').waitFor({ state: 'visible', timeout: 10000 });

        await this.page.waitForTimeout(2000);
    }

    async isMapVisible() {
        return await this.mapContainer.isVisible();
    }

    async isMapInteractive() {
        if (!this.mapFrame) {
            return false;
        }
        try {
            const container = this.mapFrame.locator('.leaflet-container');
            const hasLeafletClass = await container.evaluate(el =>
                el.classList.contains('leaflet-container')
            );
            return hasLeafletClass;
        } catch (e) {
            return false;
        }
    }
}

module.exports = { RealEstateMapPage };
