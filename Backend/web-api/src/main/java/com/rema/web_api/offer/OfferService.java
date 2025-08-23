package com.rema.web_api.offer;

import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        List<Offer> savedOffers = new ArrayList<>();

        try {

            InputStreamReader reader = new InputStreamReader(
                    getClass().getResourceAsStream("/offers.csv")
            );
            CSVReader csvReader = new CSVReader(reader);

            String[] line;
            boolean headerLine = true;

            while ((line = csvReader.readNext()) != null) {

                if (headerLine) {

                    headerLine = false;
                    continue;
                }

                String offerId = line[6];

                Optional<Offer> existingOffer = offerRepository.findById(offerId);
                if (existingOffer.isPresent()) {
                    continue;
                }

                Offer offer = Offer.builder()
                        .offerId(line[6])
                        .city(line[0])
                        .street(line[1])
                        .price_pln(Integer.valueOf(line[2]))
                        .size_m2(line[3])
                        .rooms(line[4])
                        .floor(line[5])
                        .image_url(line[7])
                        .year_built(Integer.valueOf(line[8]))
                        .market(line[9])
                        .heating(line[10])
                        .total_floors(Integer.valueOf(line[11]))
                        .intercom(Integer.valueOf(line[12]))
                        .basement(Integer.valueOf(line[13]))
                        .furnished(Integer.valueOf(line[14]))
                        .elevator(Integer.valueOf(line[15]))
                        .parkingSpace(Integer.valueOf(line[16]))
                        .gatedProperty(Integer.valueOf(line[17]))
                        .balcony(Integer.valueOf(line[18]))
                        .terrace(Integer.valueOf(line[19]))
                        .garden(Integer.valueOf(line[20]))
                        .build();

                offerRepository.save(offer);
                savedOffers.add(offer);
            }

            csvReader.close();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }

        return savedOffers;
    }
}
