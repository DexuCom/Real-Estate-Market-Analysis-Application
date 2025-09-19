package com.rema.web_api.offer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OfferMapPointDTO {

    private int id;
    private float x;
    private float y;

    private float pm2;

}


