package com.rema.web_api.user;

import com.rema.web_api.JWT.JWTService;
import com.rema.web_api.email.EmailService;
import com.rema.web_api.enums.Role;
import com.rema.web_api.token.VerificationToken;
import com.rema.web_api.token.VerificationTokenRepository;
import com.rema.web_api.user.dto.UserLoginRequestDTO;
import com.rema.web_api.user.dto.UserRegistrationRequestDTO;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    public UserService(UserRepository _userRepository, PasswordEncoder _passwordEncoder, JWTService _jwtService,
                        VerificationTokenRepository _verificationTokenRepository, EmailService _emailService) {

        this.userRepository = _userRepository;
        this.passwordEncoder = _passwordEncoder;
        this.jwtService = _jwtService;
        this.emailService = _emailService;
        this.verificationTokenRepository = _verificationTokenRepository;
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
                .enabled(false)
                .build();

        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(savedUser);
        verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
        verificationTokenRepository.save(verificationToken);

        String link = "http://localhost:8080/api/v1/auth/verify?token=" + token;
        emailService.sendVerificationEmail(savedUser.getEmail(), link);

        return savedUser;
    }

    public Optional<User> getUserById(UUID id)
    {
        return userRepository.findById(id);
    }

    public String loginUser(UserLoginRequestDTO userLoginRequestDTO)
    {
        User user = userRepository.findByUsername(userLoginRequestDTO.username()).get();

        if(BCrypt.checkpw(userLoginRequestDTO.password(), user.getPasswordHash()))
        {
            String jwtToken = jwtService.generateToken(user);
            return jwtToken;
        }
        else
        {
            throw new IllegalStateException("Niepoprawne haslo");
        }


    }
}
