package com.rema.web_api.offer;

import jakarta.persistence.*;
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
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    private String city;
    private String street;
    private Integer pricePln;
    private String sizeM2;
    private String rooms;
    private String floor;
    @Column(length = 1000)
    private String imageUrl;
    @Column(length = 1000)
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
