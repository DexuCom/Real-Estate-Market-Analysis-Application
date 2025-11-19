package com.rema.web_api.watch_list;

import com.rema.web_api.global.GlobalErrorHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@EnableWebMvc
class WatchListControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WatchListService watchListService;

    @InjectMocks
    private WatchListController watchListController;

    private UUID userId;
    private String offerId;
    private WatchList watchList;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders.standaloneSetup(watchListController)
                .setControllerAdvice(new GlobalErrorHandler())
                .build();

        userId = UUID.randomUUID();
        offerId = "1";

        WatchListId watchListId = new WatchListId(userId, offerId);
        watchList = new WatchList();
        watchList.setId(watchListId);
        watchList.setAddedAt(LocalDateTime.now());
    }

    @Test
    void addToWatchlist_Success() throws Exception {
        when(watchListService.addWatchList(any(UUID.class), anyString()))
                .thenReturn(watchList);

        mockMvc.perform(post("/api/watchLists/add")
                        .param("userId", userId.toString())
                        .param("offerId", offerId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(watchListService).addWatchList(userId, offerId);
    }

    @Test
    void addToWatchlist_Failure() throws Exception {
        // Given
        String errorMessage = "Użytkownik już ma taką ofertę zapisaną na liście obserwowanych";
        when(watchListService.addWatchList(any(UUID.class), anyString()))
                .thenThrow(new IllegalStateException(errorMessage));

        // When & Then
        mockMvc.perform(post("/api/watchLists/add")
                        .param("userId", userId.toString())
                        .param("offerId", offerId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteFromWatchList_Success() throws Exception {
        doNothing().when(watchListService).deleteFromWatchList(any(UUID.class), anyString());

        mockMvc.perform(delete("/api/watchLists/remove")
                        .param("userId", userId.toString())
                        .param("offerId", offerId))
                .andExpect(status().isNoContent());

        verify(watchListService).deleteFromWatchList(userId, offerId);
    }

    @Test
    void deleteFromWatchList_Failure() throws Exception {
        String errorMessage = "Nie ma tej oferty na liście użytkownika";
        doThrow(new IllegalStateException(errorMessage))
                .when(watchListService).deleteFromWatchList(any(UUID.class), anyString());

        mockMvc.perform(delete("/api/watchLists/remove")
                        .param("userId", userId.toString())
                        .param("offerId", offerId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getAllFromUser_Success() throws Exception {
        when(watchListService.getAllWatchListElements(any(UUID.class)))
                .thenReturn(Arrays.asList(watchList));

        mockMvc.perform(get("/api/watchLists/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].offerId").value(offerId))
                .andExpect(jsonPath("$[0].addedAt").exists());

        verify(watchListService).getAllWatchListElements(userId);
    }

    @Test
    void getAllFromUser_Failure() throws Exception {
        String errorMessage = "Użytkownik nie ma nic na swojej liście obserwowanych nieruchomości";
        when(watchListService.getAllWatchListElements(any(UUID.class)))
                .thenThrow(new IllegalStateException(errorMessage));

        mockMvc.perform(get("/api/watchLists/{userId}", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.status").value(400));
    }
}