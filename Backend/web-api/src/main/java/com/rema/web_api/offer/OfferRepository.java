package com.rema.web_api.offer;

import org.springframework.data.jpa.repository.JpaRepository;


public interface OfferRepository extends JpaRepository<Offer, Integer> {
    boolean existsByDetailUrl(String detailUrl);

}
