package com.rema.web_api.offer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OfferPricePredictionRequest {
    private Integer pricePln;
    private String sizeM2;
    private String rooms;
    private String floor;
    private String imageUrl;

    private String detailUrl;
    private Integer yearBuilt;
    private String market;
    private String heating;
    private Integer totalFloors;
    private Integer intercom;
    private Integer basement;
    private Integer furnished;
    private Integer elevator;
    private Integer parkingSpace;
    private Integer gatedProperty;
    private Integer balcony;
    private Integer terrace;
    private Integer garden;

    private double longitude;
    private double latitude;
}
