package com.rema.watch_list.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WatchListId implements Serializable {

    private UUID userId;
    private UUID offerId;
}
