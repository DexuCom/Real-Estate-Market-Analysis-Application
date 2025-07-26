package com.rema.watch_list.controller;

import com.rema.watch_list.service.WatchListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/watchlist")
public class WatchListController {

    @Autowired
    private WatchListService service;

    @PostMapping("/add")
    public void add(@RequestParam UUID userId, @RequestParam UUID offerId) {

    }
}
