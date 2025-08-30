package com.rema.web_api.user;


import com.rema.web_api.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String username;
    private String passwordHash;
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;

}
