package com.rema.web_api.user;


import com.rema.web_api.enums.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
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
