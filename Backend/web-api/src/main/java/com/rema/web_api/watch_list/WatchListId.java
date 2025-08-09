package com.rema.web_api.watch_list;

import jakarta.persistence.Embeddable;
import lombok.*;

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
