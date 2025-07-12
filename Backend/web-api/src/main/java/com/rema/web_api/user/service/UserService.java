package com.rema.web_api.user.service;

import com.rema.web_api.user.dto.UserRegistrationRequestDTO;
import com.rema.web_api.user.model.User;
import com.rema.web_api.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
                .build();


        return userRepository.save(user);
    }

}
