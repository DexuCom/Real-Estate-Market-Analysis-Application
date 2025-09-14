package com.rema.web_api.offer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class OfferMapPointDTO {

    private UUID id;
    private double longitude;
    private double latitude;

}


