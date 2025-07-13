package com.rema.web_api.user;

import com.rema.web_api.enums.Role;
import com.rema.web_api.user.dto.UserRegistrationRequestDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository _userRepository, PasswordEncoder _passwordEncoder)
    {
        this.userRepository = _userRepository;
        this.passwordEncoder = _passwordEncoder;
    }


    public User registerUser(UserRegistrationRequestDTO registerRequest)
    {
        if(userRepository.findByUsername(registerRequest.username()).isPresent())
        {
            throw new IllegalStateException("Użytkownik o takiej nazwie już istnieje!");
        }
        if(userRepository.findByEmail(registerRequest.username()).isPresent())
        {
            throw new IllegalStateException("Użytkownik o takim emailu już istnieje!");
        }


        User user = User.builder()
                .username(registerRequest.username())
                .email(registerRequest.email())
                .passwordHash(passwordEncoder.encode(registerRequest.password()))
                .role(Role.USER)
                .build();


        return userRepository.save(user);
    }

    public Optional<User> getUserById(UUID id)
    {
        return userRepository.findById(id);
    }

}
