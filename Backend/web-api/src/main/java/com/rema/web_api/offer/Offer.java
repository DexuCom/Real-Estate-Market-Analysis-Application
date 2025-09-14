package com.rema.web_api.offer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "offer")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    @Id
    private UUID id;

    private String city;
    private String street;
    private Integer price_pln;
    private String size_m2;
    private String rooms;
    private String floor;
    @Column(length = 1000)
    private String image_url;
    @Column(length = 1000)
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

    private double longitude;
    private double latitude;
}
