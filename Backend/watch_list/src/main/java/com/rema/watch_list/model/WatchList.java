package com.rema.watch_list.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="watch-list")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchList {
    @Id
    private UUID userId;
    @Id
    private UUID offerId;

    private LocalDateTime addedAt = LocalDateTime.now();
}
