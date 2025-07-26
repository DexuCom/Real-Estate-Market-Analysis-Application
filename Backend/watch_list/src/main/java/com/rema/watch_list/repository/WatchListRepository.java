package com.rema.watch_list.repository;

import com.rema.watch_list.model.WatchList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WatchListRepository extends JpaRepository<WatchList, UUID> {
    Optional<WatchList> findByUserId(UUID userId);
}
