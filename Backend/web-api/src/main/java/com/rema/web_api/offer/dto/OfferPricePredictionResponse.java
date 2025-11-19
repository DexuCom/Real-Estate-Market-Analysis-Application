package com.rema.web_api.offer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OfferPricePredictionResponse {
    @JsonProperty("predictedPricePln")
    private float predictedPricePln;
}
