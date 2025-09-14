package com.rema.web_api.offer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OfferDTO {

    private String offerId;
    private String city;
    private String street;
    private Integer price_pln;
    private String size_m2;
    private String rooms;
    private String floor;
    private String image_url;
    private String detail_url;
    private Integer year_built;
    private String market;
    private String heating;
    private Integer total_floors;
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
