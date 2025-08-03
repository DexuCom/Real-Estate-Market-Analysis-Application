package com.rema.watch_list.repository;

import com.rema.watch_list.model.WatchList;
import com.rema.watch_list.model.WatchListId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WatchListRepository extends JpaRepository<WatchList, WatchListId> {
    List<WatchList> findByIdUserId(UUID userId);
    List<WatchList> findByIdOfferId(UUID offerId);
    Optional<WatchList> findByIdUserIdAndIdOfferId(UUID userId, UUID offerId);
}
