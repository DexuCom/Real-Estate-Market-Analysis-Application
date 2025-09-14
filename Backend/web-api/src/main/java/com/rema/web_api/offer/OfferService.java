package com.rema.web_api.offer;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.rema.web_api.offer.dto.OfferMapPointDTO;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OfferService {

    private final OfferRepository offerRepository;

    public OfferService(OfferRepository _offerRepository) {
        this.offerRepository = _offerRepository;
    }

    @PostConstruct
    private void init() throws IOException {
        this.registerOffers();
    }

    public Optional<Offer> getOffer(UUID offerId) {
        return offerRepository.findById(offerId);
    }

    public List<Offer> registerOffers() throws IOException {

        offerRepository.deleteAll();

        List<Offer> savedOffers = new ArrayList<>();

        try (BOMInputStream bomInputStream = new BOMInputStream(new ClassPathResource("offers.csv").getInputStream());
             Reader reader = new InputStreamReader(bomInputStream, StandardCharsets.UTF_8)) {


            CsvToBean<OfferCsv> csvToBean = new CsvToBeanBuilder<OfferCsv>(reader)
                    .withType(OfferCsv.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            List<OfferCsv> offerCsvList = csvToBean.parse();

            for (OfferCsv offerCsv : offerCsvList) {
                Offer offer = Offer.builder()
                        .city(offerCsv.getCity())
                        .street(offerCsv.getStreet())
                        .pricePln(offerCsv.getPricePln())
                        .sizeM2(offerCsv.getSize_m2())
                        .rooms(offerCsv.getRooms())
                        .floor(offerCsv.getFloor())
                        .imageUrl(offerCsv.getImageUrl())
                        .detailUrl(offerCsv.getDetailUrl())
                        .yearBuilt(offerCsv.getYearBuilt())
                        .market(offerCsv.getMarket())
                        .heating(offerCsv.getHeating())
                        .totalFloors(offerCsv.getTotalFloors())
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


    public List<OfferMapPointDTO> getAllMapPoints() {
        List<Offer> offers = offerRepository.findAll();
        return offers.stream().map(OfferMappers::mapToOfferMapPointDTO).toList();

    }
}
