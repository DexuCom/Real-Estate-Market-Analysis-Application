package com.rema.watch_list.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class WatchListDTO {
    private UUID userId;
    private UUID offerId;
    private String addedAt;
}
