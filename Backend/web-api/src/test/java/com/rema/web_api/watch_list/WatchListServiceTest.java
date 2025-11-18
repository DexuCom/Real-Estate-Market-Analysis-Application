package com.rema.web_api.watch_list;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WatchListServiceTest {

    @Mock
    private WatchListRepository watchListRepository;

    @InjectMocks
    private WatchListService watchListService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addWatchList_Success() {
        UUID userId = UUID.randomUUID();
        String offerId = "offer123";
        WatchListId id = new WatchListId(userId, offerId);

        when(watchListRepository.findById(id)).thenReturn(Optional.empty());

        WatchList savedWatchList = WatchList.builder()
                .id(id)
                .addedAt(LocalDateTime.now())
                .build();

        when(watchListRepository.save(any(WatchList.class))).thenReturn(savedWatchList);

        WatchList result = watchListService.addWatchList(userId, offerId);

        assertEquals(id, result.getId());
        verify(watchListRepository).save(any(WatchList.class));
    }

    @Test
    void addWatchList_Failure() {
        UUID userId = UUID.randomUUID();
        String offerId = "offer123";
        WatchListId id = new WatchListId(userId, offerId);

        when(watchListRepository.findById(id)).thenReturn(Optional.of(new WatchList()));

        assertThrows(IllegalStateException.class, () -> watchListService.addWatchList(userId, offerId));
    }

    @Test
    void deleteFromWatchList_Success() {
        UUID userId = UUID.randomUUID();
        String offerId = "offer123";
        WatchListId id = new WatchListId(userId, offerId);

        when(watchListRepository.findById(id)).thenReturn(Optional.of(new WatchList()));

        watchListService.deleteFromWatchList(userId, offerId);

        verify(watchListRepository).deleteById(id);
    }

    @Test
    void deleteFromWatchList_Failure() {
        UUID userId = UUID.randomUUID();
        String offerId = "offer123";
        WatchListId id = new WatchListId(userId, offerId);

        when(watchListRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> watchListService.deleteFromWatchList(userId, offerId));
    }

    @Test
    void getAllWatchListElements_Success() {
        UUID userId = UUID.randomUUID();
        WatchList watchList = WatchList.builder()
                .id(new WatchListId(userId, "offer1"))
                .addedAt(LocalDateTime.now())
                .build();

        when(watchListRepository.findByIdUserId(userId)).thenReturn(List.of(watchList));

        List<WatchList> result = watchListService.getAllWatchListElements(userId);

        assertEquals(1, result.size());
        assertEquals("offer1", result.get(0).getId().getOfferId());
    }

    @Test
    void getAllWatchListElements_Failure() {
        UUID userId = UUID.randomUUID();

        when(watchListRepository.findByIdUserId(userId)).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> watchListService.getAllWatchListElements(userId));
    }
}