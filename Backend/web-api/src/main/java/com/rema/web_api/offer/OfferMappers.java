package com.rema.web_api.offer;

import com.rema.web_api.offer.dto.OfferMapPointDTO;

public class OfferMappers {

    public static OfferMapPointDTO mapToOfferMapPointDTO(Offer offer) {
        return OfferMapPointDTO.builder()
                .id(offer.getId())
                .y(offer.getLatitude())
                .x(offer.getLongitude())
                .pm2((float) (offer.getPricePln()) / offer.getSizeM2()).build();
    }
}
