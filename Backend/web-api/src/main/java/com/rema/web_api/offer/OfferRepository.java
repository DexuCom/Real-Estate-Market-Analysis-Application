package com.rema.web_api.offer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, String> {
    boolean existsByDetailUrl(String detailUrl);

}
