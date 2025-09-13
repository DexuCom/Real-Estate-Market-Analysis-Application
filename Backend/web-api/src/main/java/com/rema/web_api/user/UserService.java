package com.rema.web_api.user;

import com.rema.web_api.JWT.JWTService;
import com.rema.web_api.enums.Role;
import com.rema.web_api.user.dto.UserLoginRequestDTO;
import com.rema.web_api.user.dto.UserRegistrationRequestDTO;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public UserService(UserRepository _userRepository, PasswordEncoder _passwordEncoder, JWTService _jwtService)
    {
        this.userRepository = _userRepository;
        this.passwordEncoder = _passwordEncoder;
        this.jwtService = _jwtService;
    }


    public User registerUser(UserRegistrationRequestDTO registerRequest)
    {
        if(userRepository.findByUsername(registerRequest.username()).isPresent())
        {
            throw new IllegalStateException("Użytkownik o takiej nazwie już istnieje!");
        }
        if(userRepository.findByEmail(registerRequest.email()).isPresent())
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

    public String loginUser(UserLoginRequestDTO userLoginRequestDTO)
    {
        Optional<User> userOptional = userRepository.findByUsername(userLoginRequestDTO.username());
        
        if(userOptional.isEmpty())
        {
            throw new IllegalStateException("Niepoprawne dane logowania");
        }
        
        User user = userOptional.get();

        if(BCrypt.checkpw(userLoginRequestDTO.password(), user.getPasswordHash()))
        {
            String jwtToken = jwtService.generateToken(user);
            return jwtToken;
        }
        else
        {
            throw new IllegalStateException("Niepoprawne dane logowania");
        }
    }
}
