package com.rema.web_api.offer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferMapPointRequest {
    private String market;
    private Integer pricePlnFrom;
    private Integer pricePlnTo;

    private Float pm2From;
    private Float pm2To;

    private Integer roomsFrom;
    private Integer roomsTo;
    private Integer floorsFrom;
    private Integer floorsTo;

    private Float sizeM2From;
    private Float sizeM2To;

    private Integer yearBuiltFrom;
    private Integer yearBuiltTo;
}
