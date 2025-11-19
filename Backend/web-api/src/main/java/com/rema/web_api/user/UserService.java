package com.rema.web_api.user;

import com.rema.web_api.JWT.JWTService;
import com.rema.web_api.email.EmailService;
import com.rema.web_api.enums.Role;
import com.rema.web_api.token.PasswordResetToken;
import com.rema.web_api.token.PasswordResetTokenRepository;
import com.rema.web_api.token.VerificationToken;
import com.rema.web_api.token.VerificationTokenRepository;
import com.rema.web_api.user.dto.UserLoginRequestDTO;
import com.rema.web_api.user.dto.UserRegistrationRequestDTO;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserService(UserRepository _userRepository, PasswordEncoder _passwordEncoder, JWTService _jwtService,
                        VerificationTokenRepository _verificationTokenRepository, EmailService _emailService,
                        PasswordResetTokenRepository _passwordResetTokenRepository) {

        this.userRepository = _userRepository;
        this.passwordEncoder = _passwordEncoder;
        this.jwtService = _jwtService;
        this.emailService = _emailService;
        this.verificationTokenRepository = _verificationTokenRepository;
        this.passwordResetTokenRepository = _passwordResetTokenRepository;
    }

    @Transactional
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

        String link = "http://localhost:8080/api/users/verify?token=" + token;
        emailService.sendVerificationEmail(savedUser.getEmail(), link);

        return savedUser;
    }

    @Transactional
    public Optional<User> getUserById(UUID id)
    {
        return userRepository.findById(id);
    }

    @Transactional
    public String loginUser(UserLoginRequestDTO userLoginRequestDTO)
    {
        Optional<User> userOptional = userRepository.findByUsername(userLoginRequestDTO.username());
        
        if(userOptional.isEmpty())
        {
            throw new IllegalStateException("Nie istnieje uzytkownik o takim loginie");
        }
        
        User user = userOptional.get();

        if (user.getEnabled() == false) {
            throw new IllegalStateException("Konto użytkownika nie jest aktywowane");
        }

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

    @Transactional
    public User verifyUser(String token) {
        Optional<VerificationToken> verificationTokenOptional = verificationTokenRepository.findByToken(token);
        if (verificationTokenOptional.isEmpty()) {
            throw new IllegalStateException("Nieprawidłowy token weryfikacyjny!");
        }
        VerificationToken verificationToken = verificationTokenOptional.get();
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token weryfikacyjny wygasł! Proszę zarejestrować się ponownie.");
        }
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        return user;
    }

    @Transactional
    public String forgotPassword(String email) {
        
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new IllegalStateException("Nie znaleziono użytkownika z podanym adresem email!");
        }
        User user = userOptional.get();

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://localhost:8080/api/users/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        return resetLink;
    }

    @Transactional
    public User resetPassword(String token, String newPassword) {
        
        Optional<PasswordResetToken> resetTokenOptional = passwordResetTokenRepository.findByToken(token);
        
        if (resetTokenOptional.isEmpty()) {
            throw new IllegalStateException("Nieprawidłowy token do resetowania hasła!");
        }

        PasswordResetToken resetToken = resetTokenOptional.get();
        
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token do resetowania hasła wygasł!");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        return user;
    }
}
