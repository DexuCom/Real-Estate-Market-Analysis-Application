package com.rema.web_api.offer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OfferDTO {
    private int id;

    private String city;
    private String street;
    private Integer pricePln;
    private Float sizeM2;
    private Integer rooms;
    private Integer floor;

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

}
