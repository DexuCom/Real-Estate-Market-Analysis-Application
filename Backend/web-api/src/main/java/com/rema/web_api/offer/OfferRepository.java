package com.rema.web_api.offer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {
    boolean existsByDetailUrl(String detailUrl);

}
