package com.rema.web_api.offer;

import com.rema.web_api.offer.dto.OfferMapPointDTO;
import com.rema.web_api.offer.dto.OfferMapPointRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @InjectMocks
    private OfferService offerService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getOffer_Success() {
        Offer offer = Offer.builder().id(1).city("Warsaw").build();

        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));

        Optional<Offer> result = offerService.getOffer(1);

        assertTrue(result.isPresent());
        assertEquals("Warsaw", result.get().getCity());
    }

    @Test
    void getOffer_Failure() {
        when(offerRepository.findById(123)).thenReturn(Optional.empty());

        Optional<Offer> result = offerService.getOffer(123);

        assertFalse(result.isPresent());
    }

    @Test
    void getAllMapPoints() {

        Offer offer = Offer.builder()
                .id(1)
                .pricePln(500000)
                .sizeM2(50f)
                .latitude(52.1f)
                .longitude(21.0f)
                .build();

        when(offerRepository.findAll(any(Specification.class))).thenReturn(List.of(offer));

        List<OfferMapPointDTO> result = offerService.getAllMapPoints(OfferMapPointRequest.builder().build());

        assertEquals(1, result.size());
        assertEquals(21.0f, result.get(0).getX());
        assertEquals(52.1f, result.get(0).getY());
        assertEquals(10000f, result.get(0).getPm2());
    }
}
