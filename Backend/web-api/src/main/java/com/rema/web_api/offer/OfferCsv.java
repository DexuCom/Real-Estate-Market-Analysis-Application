package com.rema.web_api.offer;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class OfferCsv {


    @CsvBindByName(column = "city")
    private String city;

    @CsvBindByName(column = "street")
    private String street;

    @CsvBindByName(column = "price_pln")
    private Integer pricePln;

    @CsvBindByName(column = "size_m2")
    private Float size_m2;

    @CsvBindByName(column = "rooms")
    private String rooms;

    @CsvBindByName(column = "floor")
    private String floor;

    @CsvBindByName(column = "image_url")
    private String imageUrl;
    @CsvBindByName(column = "detail_url")
    private String detailUrl;
    @CsvBindByName(column = "year_built")
    private Integer yearBuilt;

    @CsvBindByName(column = "market")
    private String market;

    @CsvBindByName(column = "heating")
    private String heating;

    @CsvBindByName(column = "total_floors")
    private Integer totalFloors;

    @CsvBindByName(column = "intercom")
    private Integer intercom;

    @CsvBindByName(column = "basement")
    private Integer basement;

    @CsvBindByName(column = "furnished")
    private Integer furnished;

    @CsvBindByName(column = "elevator")
    private Integer elevator;

    @CsvBindByName(column = "parking_space")
    private Integer parkingSpace;

    @CsvBindByName(column = "gated_property")
    private Integer gatedProperty;

    @CsvBindByName(column = "balcony")
    private Integer balcony;

    @CsvBindByName(column = "terrace")
    private Integer terrace;

    @CsvBindByName(column = "garden")
    private Integer garden;

    @CsvBindByName(column = "latitude")
    private float latitude;

    @CsvBindByName(column = "longitude")
    private float longitude;
}