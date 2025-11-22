package com.rema.web_api.offer;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.rema.web_api.offer.dto.OfferMapPointDTO;
import com.rema.web_api.offer.dto.OfferMapPointRequest;
import com.rema.web_api.offer.dto.OfferPricePredictionResponse;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final WebClient webClient;

    private final String PREDICTION_URL_SUFFIX = "api/scoring-model/predict/";

    public OfferService(OfferRepository _offerRepository, WebClient _webClient) {
        this.offerRepository = _offerRepository;
        this.webClient = _webClient;
    }

    @PostConstruct
    private void init() throws IOException {
        this.registerOffers();
    }


    public Optional<Offer> getOffer(Integer offerId) {
        return offerRepository.findById(offerId);
    }

    @Transactional
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


    public List<OfferMapPointDTO> getAllMapPoints(OfferMapPointRequest request) {
        Specification<Offer> spec = OfferSpecification.offerFilters(request);
        List<Offer> offers = offerRepository.findAll(spec);
        return offers.stream().map(OfferMappers::mapToOfferMapPointDTO).toList();

    }

    public OfferFilterRanges getOfferFilterRanges() {
        return offerRepository.getOfferFilterRanges();
    }


    public OfferPricePredictionResponse predictPriceForOfferById(Integer id, String model) {
        Optional<Offer> offer = offerRepository.findById(id);
        if (offer.isEmpty()) {
            throw new NoSuchElementException();
        }

        return predictPriceForOffer(offer.get(), model);
    }

    public OfferPricePredictionResponse predictPriceForOffer(Offer offer, String model) {
        String predictionUri = PREDICTION_URL_SUFFIX + model;

        try {
            return webClient.post()
                    .uri(predictionUri)
                    .body(Mono.just(offer), Offer.class)
                    .retrieve()
                    .bodyToMono(OfferPricePredictionResponse.class)
                    .block();

        } catch (Exception e) {
            throw new IllegalStateException("Unable to get prediction from scoring model api: " + e.getMessage());
        }

    }


}
