package com.rema.web_api.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


public interface OfferRepository extends JpaRepository<Offer, Integer>, JpaSpecificationExecutor<Offer> {
    @Query("""
                SELECT 
                    MIN(o.pricePln) as minPricePln, 
                    MAX(o.pricePln) as maxPricePln,
                    
                    MIN(o.sizeM2) as minSizeM2, 
                    MAX(o.sizeM2) as maxSizeM2,
                                
                    MIN(CASE WHEN o.yearBuilt > 0 THEN o.yearBuilt END) as minYearBuilt,
                    MAX(CASE WHEN o.yearBuilt > 0 THEN o.yearBuilt END) as maxYearBuilt,
                    
                    MIN(o.pricePln / o.sizeM2) as minPm2,
                    MAX(o.pricePln / o.sizeM2) as maxPm2,
                    
                    MIN(o.rooms) as minRooms,
                    MAX(o.rooms) as maxRooms,
                    
                    MIN(o.floor) as minFloor,
                    MAX(o.floor) as maxFloor
                FROM Offer o
            """)
    OfferFilterRanges getOfferFilterRanges();

    boolean existsByDetailUrl(String detailUrl);

}
