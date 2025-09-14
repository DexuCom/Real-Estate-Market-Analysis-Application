package com.rema.web_api.offer;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OfferCsv {



    @CsvBindByName(column = "city")
    private String city;

    @CsvBindByName(column = "street")
    private String street;

    @CsvBindByName(column = "price_pln")
    private Integer price_pln;

    @CsvBindByName(column = "size_m2")
    private String size_m2;

    @CsvBindByName(column = "rooms")
    private String rooms;

    @CsvBindByName(column = "floor")
    private String floor;

    @CsvBindByName(column = "image_url")
    private String image_url;
    @CsvBindByName(column = "detail_url")
    private String detail_url;
    @CsvBindByName(column = "year_built")
    private Integer year_built;

    @CsvBindByName(column = "market")
    private String market;

    @CsvBindByName(column = "heating")
    private String heating;

    @CsvBindByName(column = "total_floors")
    private Integer total_floors;

    @CsvBindByName(column = "intercom")
    private Integer intercom;

    @CsvBindByName(column = "basement")
    private Integer basement;

    @CsvBindByName(column = "furnished")
    private Integer furnished;

    @CsvBindByName(column = "elevator")
    private Integer elevator;

    @CsvBindByName(column = "parkingSpace")
    private Integer parkingSpace;

    @CsvBindByName(column = "gatedProperty")
    private Integer gatedProperty;

    @CsvBindByName(column = "balcony")
    private Integer balcony;

    @CsvBindByName(column = "terrace")
    private Integer terrace;

    @CsvBindByName(column = "garden")
    private Integer garden;

    @CsvBindByName(column = "latitude")
    private BigDecimal latitude;

    @CsvBindByName(column = "longitude")
    private BigDecimal longitude;
}