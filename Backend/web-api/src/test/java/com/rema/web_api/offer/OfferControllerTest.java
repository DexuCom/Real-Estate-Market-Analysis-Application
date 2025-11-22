package com.rema.web_api.offer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rema.web_api.offer.dto.OfferMapPointDTO;
import com.rema.web_api.offer.dto.OfferMapPointRequest;
import com.rema.web_api.offer.dto.OfferPricePredictionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@EnableWebMvc
class OfferControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OfferService offerService;

    @InjectMocks
    private OfferController offerController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Offer testOffer;
    private OfferMapPointDTO testMapPoint;
    private OfferPricePredictionResponse testPrediction;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(offerController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        testOffer = Offer.builder()
                .id(1)
                .city("Gdańsk")
                .street("Testowa")
                .pricePln(500000)
                .sizeM2(50.0f)
                .rooms("2")
                .floor("3")
                .imageUrl("http://test.com/image/1")
                .detailUrl("http://test.com/offer/1")
                .yearBuilt(2020)
                .market("pierwotny")
                .heating("miejskie")
                .totalFloors(10)
                .intercom(1)
                .basement(0)
                .furnished(1)
                .elevator(1)
                .parkingSpace(1)
                .gatedProperty(1)
                .balcony(1)
                .terrace(0)
                .garden(0)
                .latitude(54.372158f)
                .longitude(18.638306f)
                .build();

        testMapPoint = OfferMapPointDTO.builder()
                .id(1)
                .x(18.638306f)
                .y(54.372158f)
                .pm2(10000.0f)
                .build();

        testPrediction = OfferPricePredictionResponse.builder()
                .predictedPricePln(520000.0f)
                .build();
    }

    @Test
    void addData_Success() throws Exception {
        when(offerService.registerOffers()).thenReturn(Arrays.asList(testOffer));

        mockMvc.perform(post("/api/offer/add"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(offerService).registerOffers();
    }

    @Test
    void addData_Failure() throws Exception {
        when(offerService.registerOffers())
                .thenThrow(new IllegalStateException("Failed to parse CSV file"));

        mockMvc.perform(post("/api/offer/add"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to parse CSV file"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getOffer_Success() throws Exception {
        when(offerService.getOffer(1)).thenReturn(Optional.of(testOffer));

        mockMvc.perform(get("/api/offer/show")
                        .param("offerId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.city").value("Gdańsk"))
                .andExpect(jsonPath("$.street").value("Testowa"))
                .andExpect(jsonPath("$.pricePln").value(500000));
    }

    @Test
    void getOffer_NotFound() throws Exception {
        when(offerService.getOffer(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/offer/show")
                        .param("offerId", "999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Offer not found in database."))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAllMapPoints_Success() throws Exception {
        when(offerService.getAllMapPoints(any(OfferMapPointRequest.class)))
                .thenReturn(Arrays.asList(testMapPoint));

        mockMvc.perform(post("/api/offer/map-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].x").value(18.638306))
                .andExpect(jsonPath("$[0].y").value(54.372158))
                .andExpect(jsonPath("$[0].pm2").value(10000.0));

    }

    @Test
    void getAllMapPoints_Failure() throws Exception {
        when(offerService.getAllMapPoints(any(OfferMapPointRequest.class))).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/offer/map-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getPredictionForOfferById_Success() throws Exception {
        when(offerService.predictPriceForOfferById(1, "xgb")).thenReturn(testPrediction);

        mockMvc.perform(get("/api/offer/predict-price/1")
                        .param("model", "xgb"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predictedPricePln").value(520000.0));  // Updated to match actual field name
    }

    @Test
    void getPredictionForOfferById_NotFound() throws Exception {
        when(offerService.predictPriceForOfferById(999, "xgb"))
                .thenThrow(new NoSuchElementException("Offer not found"));

        mockMvc.perform(get("/api/offer/predict-price/999")
                        .param("model", "xgb"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getPredictionForOfferById_ServiceError() throws Exception {
        when(offerService.predictPriceForOfferById(1, "xgb"))
                .thenThrow(new IllegalStateException("Scoring service unavailable"));

        mockMvc.perform(get("/api/offer/predict-price/1")
                        .param("model", "xgb"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("\"Scoring service unavailable\""));
    }
}