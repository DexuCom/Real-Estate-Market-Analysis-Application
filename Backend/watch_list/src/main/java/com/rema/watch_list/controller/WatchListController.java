package com.rema.watch_list.controller;

import com.rema.watch_list.model.WatchList;
import com.rema.watch_list.model.dto.ErrorDTO;
import com.rema.watch_list.model.dto.WatchListDTO;
import com.rema.watch_list.service.WatchListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/watchLists")
public class WatchListController {

    @Autowired
    private WatchListService service;

    @PostMapping("/add")
    public ResponseEntity<?> addToWatchlist(@RequestParam UUID userId, @RequestParam UUID offerId) {
        try {

            WatchList watchList = service.addWatchList(userId, offerId);

            return ResponseEntity.ok().build();
        }
        catch (IllegalStateException e) {

            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value());

            return ResponseEntity
                    .badRequest()
                    .body(errorDTO);
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> deleteFromWatchList(@RequestParam UUID userId, @RequestParam UUID offerId) {
        try {

            service.deleteFromWatchList(userId, offerId);

            return ResponseEntity.noContent().build();
        }
        catch (IllegalStateException e) {

            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value());

            return ResponseEntity
                    .badRequest()
                    .body(errorDTO);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getAllFromUser(@PathVariable UUID userId) {

        try {
            List<WatchListDTO> watchListElements = service.getAllWatchListElements(userId).stream()
                    .map(element -> {

                        WatchListDTO dto = new WatchListDTO();

                        dto.setUserId(element.getId().getUserId());
                        dto.setOfferId(element.getId().getOfferId());
                        dto.setAddedAt(element.getAddedAt().toString());

                        return dto;
                    }).collect(Collectors.toList());

            return ResponseEntity.ok(watchListElements);
        }
        catch (IllegalStateException e) {
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value());

            return ResponseEntity
                    .badRequest()
                    .body(errorDTO);
        }
    }
}
