package com.rema.web_api.offer;

import com.rema.web_api.global.dto.ErrorDTO;
import com.rema.web_api.offer.dto.OfferDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/offer")
public class OfferController {

    @Autowired
    private OfferService offerService;

    @PostMapping("/add")
    public ResponseEntity<?> addData() {

        try
        {
            List<Offer> offer = offerService.registerOffers();

            return ResponseEntity.ok().build();
        }
        catch (IllegalStateException e)
        {
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value());

            return ResponseEntity.
                    badRequest()
                    .body(errorDTO);
        }
    }

    @GetMapping("/show")
    public ResponseEntity<?> getOffer(@RequestParam String offerId) {

        Optional<Offer> offerOptional = offerService.getOffer(offerId);

        if (offerOptional.isEmpty()) {

            ErrorDTO errorDTO = new ErrorDTO("Offer not found in database.", HttpStatus.NOT_FOUND.value());

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(errorDTO);
        }

        Offer offer = offerOptional.get();
        OfferDTO offerDTO = OfferDTO.builder()
                .offerId(offer.getOfferId())
                .city(offer.getCity())
                .street(offer.getStreet())
                .price_pln(offer.getPrice_pln())
                .size_m2(offer.getSize_m2())
                .rooms(offer.getRooms())
                .floor(offer.getFloor())
                .image_url(offer.getImage_url())
                .year_built(offer.getYear_built())
                .market(offer.getMarket())
                .heating(offer.getHeating())
                .total_floors(offer.getTotal_floors())
                .intercom(offer.getIntercom())
                .basement(offer.getBasement())
                .furnished(offer.getFurnished())
                .elevator(offer.getElevator())
                .parkingSpace(offer.getParkingSpace())
                .gatedProperty(offer.getGatedProperty())
                .balcony(offer.getBalcony())
                .terrace(offer.getTerrace())
                .garden(offer.getGarden())
                .build();


        return ResponseEntity.ok(offerDTO);
    }
}
