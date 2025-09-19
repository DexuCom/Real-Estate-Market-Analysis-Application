package com.rema.web_api.offer;

import com.rema.web_api.offer.dto.OfferMapPointDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OfferMappers {

    public static OfferMapPointDTO mapToOfferMapPointDTO(Offer offer) {
        BigDecimal price = BigDecimal.valueOf(offer.getPricePln());
        BigDecimal sizem2 = BigDecimal.valueOf(offer.getSizeM2());

        BigDecimal pm2 = price.divide(sizem2, 2, RoundingMode.HALF_UP);

        return OfferMapPointDTO.builder()
                .id(offer.getId())
                .y(offer.getLatitude())
                .x(offer.getLongitude())
                .pm2(pm2.floatValue()).build();
    }
}
