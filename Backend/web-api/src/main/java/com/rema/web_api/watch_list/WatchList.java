package com.rema.web_api.watch_list;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="watch_list")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchList {

    @EmbeddedId
    private WatchListId id;

    private LocalDateTime addedAt;
}
