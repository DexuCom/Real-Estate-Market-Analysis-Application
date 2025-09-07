package com.rema.web_api.user;

import com.rema.web_api.email.EmailService;
import com.rema.web_api.global.dto.ErrorDTO;
import com.rema.web_api.token.PasswordResetToken;
import com.rema.web_api.token.PasswordResetTokenRepository;
import com.rema.web_api.token.VerificationToken;
import com.rema.web_api.token.VerificationTokenRepository;
import com.rema.web_api.user.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService _userService, VerificationTokenRepository _verificationTokenRepository,
                          UserRepository _userRepository, EmailService _emailService, PasswordResetTokenRepository _passwordResetTokenRepository,
                          PasswordEncoder _passwordEncoder)
    {
        this.userService = _userService;
        this.verificationTokenRepository = _verificationTokenRepository;
        this.userRepository = _userRepository;
        this.emailService = _emailService;
        this.passwordResetTokenRepository = _passwordResetTokenRepository;
        this.passwordEncoder = _passwordEncoder;
    }

    @GetMapping("/{id}") ResponseEntity<UserDTO> getUserData(@PathVariable UUID id)
    {
        Optional<User> userOptional = userService.getUserById(id);

        if(userOptional.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        UserDTO userDTO = UserDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(userDTO);

    }


    @PostMapping("")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequestDTO registerUserDTO)
    {
        try
        {
            User userOptional = userService.registerUser(registerUserDTO);
            return ResponseEntity.ok().build();
        }
        catch (IllegalStateException e)
        {
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value());

            return ResponseEntity.
                    badRequest()
                    .body(errorDTO);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequestDTO userLoginRequestDTO)
    {
        try
        {
            String jwtToken = userService.loginUser(userLoginRequestDTO);
            UserJwtDTO userJwtDTO = new UserJwtDTO(jwtToken);

            return ResponseEntity.ok(userJwtDTO);
        }
        catch(IllegalStateException e)
        {
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), HttpStatus.UNAUTHORIZED.value());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorDTO);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam String token) {

        Optional<VerificationToken> verificationTokenOptional = verificationTokenRepository.findByToken(token);
        if (verificationTokenOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Nieprawidłowy token weryfikacyjny!", HttpStatus.BAD_REQUEST.value()));
        }
        VerificationToken verificationToken = verificationTokenOptional.get();
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(new ErrorDTO
                    ("Token weryfikacyjny wygasł! Proszę zarejestrować się ponownie."
                    , HttpStatus.BAD_REQUEST.value()));
        }
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok().body("Konto zostało pomyślnie zweryfikowane!");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Nie znaleziono użytkownika z podanym adresem email!", HttpStatus.BAD_REQUEST.value()));
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

        return ResponseEntity.ok().body("Link do resetowania hasła został wysłany na podany adres email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {

        Optional<PasswordResetToken> resetTokenOptional = passwordResetTokenRepository.findByToken(token);
        if (resetTokenOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Nieprawidłowy token do resetowania hasła!", HttpStatus.BAD_REQUEST.value()));
        }

        PasswordResetToken resetToken = resetTokenOptional.get();
        
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(new ErrorDTO("Token do resetowania hasła wygasł!", HttpStatus.BAD_REQUEST.value()));
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        return ResponseEntity.ok().body("Hasło zostało pomyślnie zresetowane.");
    }
}
