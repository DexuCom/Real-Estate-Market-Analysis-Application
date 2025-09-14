package com.rema.web_api.offer;

import com.rema.web_api.offer.dto.OfferMapPointDTO;

public class OfferMappers {

    public static OfferMapPointDTO mapToOfferMapPointDTO(Offer offer) {
        return OfferMapPointDTO.builder()
                .id(offer.getId())
                .latitude(offer.getLatitude())
                .longitude(offer.getLongitude())
                .pricePln(offer.getPricePln()).build();
    }
}
