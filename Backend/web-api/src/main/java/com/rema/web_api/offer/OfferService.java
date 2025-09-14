package com.rema.web_api.offer;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OfferService {

    private final OfferRepository offerRepository;

    public OfferService (OfferRepository _offerRepository){
        this.offerRepository = _offerRepository;
    }

    public Optional<Offer> getOffer(String offerId) {
        return offerRepository.findById(offerId);
    }

    public List<Offer> registerOffers() {

        offerRepository.deleteAll();

        List<Offer> savedOffers = new ArrayList<>();

        try {

            InputStreamReader reader = new InputStreamReader(
                    getClass().getResourceAsStream("/offers.csv")
            );

            CsvToBean<OfferCsv> csvToBean = new CsvToBeanBuilder<OfferCsv>(reader)
                    .withType(OfferCsv.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            List<OfferCsv> offerCsvList = csvToBean.parse();

            for(OfferCsv offerCsv : offerCsvList)
            {
                Offer offer = Offer.builder()
                        .id(UUID.randomUUID())
                        .city(offerCsv.getCity())
                        .street(offerCsv.getStreet())
                        .price_pln(offerCsv.getPrice_pln())
                        .size_m2(offerCsv.getSize_m2())
                        .rooms(offerCsv.getRooms())
                        .floor(offerCsv.getFloor())
                        .image_url(offerCsv.getImage_url())
                        .detail_url(offerCsv.getDetail_url())
                        .year_built(offerCsv.getYear_built())
                        .market(offerCsv.getMarket())
                        .heating(offerCsv.getHeating())
                        .total_floors(offerCsv.getTotal_floors())
                        .intercom(offerCsv.getIntercom())
                        .basement(offerCsv.getBasement())
                        .furnished(offerCsv.getFurnished())
                        .elevator(offerCsv.getElevator())
                        .parkingSpace(offerCsv.getParkingSpace())
                        .gatedProperty(offerCsv.getGatedProperty())
                        .balcony(offerCsv.getBalcony())
                        .terrace(offerCsv.getTerrace())
                        .garden(offerCsv.getGarden())
                        .latitude(offerCsv.getLatitude())
                        .longitude(offerCsv.getLongitude())
                        .build();
                offerRepository.save(offer);
                savedOffers.add(offer);
            }

        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }

        return savedOffers;
    }
}
