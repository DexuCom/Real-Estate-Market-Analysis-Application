package com.rema.web_api.watch_list;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WatchListService {

    private final WatchListRepository watchListRepository;

    public WatchListService(WatchListRepository _watchListRepository) {
        this.watchListRepository = _watchListRepository;
    }

    public WatchList addWatchList(UUID userId, String offerId) {

        WatchListId id = new WatchListId(userId, offerId);

        if (watchListRepository.findById(id).isPresent()){
            throw new IllegalStateException("Użytkownik już ma taką ofertę zapisaną na liście obserwowanych");
        }

        WatchList watchList = WatchList.builder()
                .id(id)
                .addedAt(LocalDateTime.now())
                .build();

        return watchListRepository.save(watchList);
    }

    public void deleteFromWatchList(UUID userId, String offerId) {

        WatchListId id = new WatchListId(userId, offerId);

        if (watchListRepository.findById(id).isPresent()){
            watchListRepository.deleteById(id);
        }
        else {
            throw new IllegalStateException("Nie ma tej oferty na liście użytkownika");
        }
    }

    public List<WatchList> getAllWatchListElements(UUID userId) {

        List<WatchList> userWatchList = watchListRepository.findByIdUserId(userId);

        if(userWatchList.isEmpty()) {
            throw new IllegalStateException("Takiego użytkownika nie ma w bazie danych");
        }

        return userWatchList;
    }
}
