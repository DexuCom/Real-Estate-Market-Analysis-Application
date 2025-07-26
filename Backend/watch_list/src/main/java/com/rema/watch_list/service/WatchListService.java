package com.rema.watch_list.service;

import com.rema.watch_list.repository.WatchListRepository;
import org.springframework.stereotype.Service;

@Service
public class WatchListService {

    private final WatchListRepository watchListRepository;

    public WatchListService(WatchListRepository _watchListRepository) {
        this.watchListRepository = _watchListRepository;
    }
}
