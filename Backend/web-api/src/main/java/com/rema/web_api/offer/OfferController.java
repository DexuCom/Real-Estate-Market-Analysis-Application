package com.rema.web_api.offer;

import com.rema.web_api.global.dto.ErrorDTO;
import com.rema.web_api.offer.dto.OfferDTO;
import com.rema.web_api.offer.dto.OfferMapPointDTO;
import com.rema.web_api.offer.dto.OfferPricePredictionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/offer")
public class OfferController {

    @Autowired
    private OfferService offerService;


    @PostMapping("/add")
    public ResponseEntity<?> addData() {

        try {
            List<Offer> offer = offerService.registerOffers();

            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value());

            return ResponseEntity.
                    badRequest()
                    .body(errorDTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/show")
    public ResponseEntity<?> getOffer(@RequestParam UUID offerId) {

        Optional<Offer> offerOptional = offerService.getOffer(offerId);

        if (offerOptional.isEmpty()) {

            ErrorDTO errorDTO = new ErrorDTO("Offer not found in database.", HttpStatus.NOT_FOUND.value());

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(errorDTO);
        }

        Offer offer = offerOptional.get();
        OfferDTO offerDTO = OfferDTO.builder()
                .id(offer.getId())
                .detailUrl(offer.getDetailUrl())
                .city(offer.getCity())
                .street(offer.getStreet())
                .pricePln(offer.getPricePln())
                .sizeM2(offer.getSizeM2())
                .rooms(offer.getRooms())
                .floor(offer.getFloor())
                .imageUrl(offer.getImageUrl())
                .detailUrl(offer.getDetailUrl())
                .yearBuilt(offer.getYearBuilt())
                .market(offer.getMarket())
                .heating(offer.getHeating())
                .totalFloors(offer.getTotalFloors())
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

    @GetMapping("/map-points")
    public ResponseEntity<List<OfferMapPointDTO>> getAllMapPoints() {
        try {
            List<OfferMapPointDTO> offerMapPointDTOList = offerService.getAllMapPoints();
            return ResponseEntity.ok(offerMapPointDTOList);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/predict-price/{id}?model={model}")
    public ResponseEntity<OfferPricePredictionResponse> getPredictionForOfferById(
            @PathVariable Integer id, @RequestParam(defaultValue = "xgb") String model) {

        try {
            OfferPricePredictionResponse prediction = offerService.predictPriceForOfferById(id, model);
            return ResponseEntity.ok(prediction);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.internalServerError().build();
        }

    }

}
