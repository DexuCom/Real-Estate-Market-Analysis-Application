package com.rema.web_api.offer;

import com.rema.web_api.offer.dto.OfferMapPointDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfferMappersTest {

    @Test
    void mapToOfferMapPointDTO_Success() {
        Offer offer = Offer.builder()
                .id(1)
                .pricePln(500000)
                .sizeM2(50f)
                .latitude(52.1f)
                .longitude(21.0f)
                .build();

        OfferMapPointDTO dto = OfferMappers.mapToOfferMapPointDTO(offer);

        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals(21.0f, dto.getX());
        assertEquals(52.1f, dto.getY());
        assertEquals(10000f, dto.getPm2());
    }

    @Test
    void mapToOfferMapPointDTO_Failure() {
        Offer offer = Offer.builder()
                .id(1)
                .pricePln(null)
                .sizeM2(50f)
                .latitude(52.1f)
                .longitude(21.0f)
                .build();

        assertThrows(NullPointerException.class, () -> {
            OfferMappers.mapToOfferMapPointDTO(offer);
        });

        Offer offer2 = Offer.builder()
                .id(2)
                .pricePln(500000)
                .sizeM2(null)
                .latitude(52.1f)
                .longitude(21.0f)
                .build();

        assertThrows(NullPointerException.class, () -> {
            OfferMappers.mapToOfferMapPointDTO(offer2);
        });
    }
}
