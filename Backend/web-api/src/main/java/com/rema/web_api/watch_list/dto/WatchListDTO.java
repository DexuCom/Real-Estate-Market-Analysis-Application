package com.rema.web_api.watch_list.dto;

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
