package com.rema.web_api.offer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OfferPricePredictionResponse {
    @JsonProperty("predictedPricePln")
    private float predictedPricePln;
}
