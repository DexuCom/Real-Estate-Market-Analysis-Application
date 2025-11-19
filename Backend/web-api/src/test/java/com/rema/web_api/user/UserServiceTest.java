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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTService jwtService;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_Success() {
        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO("testuser", "test@example.com", "password123");

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        User savedUser = User.builder()
                .username(request.username())
                .email(request.email())
                .enabled(false)
                .role(Role.USER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(request);

        assertEquals(request.username(), result.getUsername());
        assertEquals(request.email(), result.getEmail());
        assertFalse(result.getEnabled());
        assertEquals(Role.USER, result.getRole());

        verify(emailService).sendVerificationEmail(eq(result.getEmail()), anyString());
        verify(verificationTokenRepository).save(any());
    }

    @Test
    void registerUser_FailureWhenUsernameExists() {
        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO("existingUser", "test@example.com", "password123");
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(new User()));

        assertThrows(IllegalStateException.class, () -> userService.registerUser(request));
    }

    @Test
    void registerUser_FailureWhenEmailExists() {
        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO("testuser", "existing@example.com", "password123");
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        assertThrows(IllegalStateException.class, () -> userService.registerUser(request));
    }

    @Test
    void loginUser_Success() {
        UserLoginRequestDTO loginRequest = new UserLoginRequestDTO("testuser", "password123");

        User user = User.builder()
                .username("testuser")
                .passwordHash("$2a$10$7q8y9z2yF6E5k6g0sQ0U1eRrGZ3uL.zLkD2nPnI7eN/dM9C6vA1bK")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        try (MockedStatic<BCrypt> bCryptMock = Mockito.mockStatic(BCrypt.class)) {
            bCryptMock.when(() -> BCrypt.checkpw("password123", user.getPasswordHash())).thenReturn(true);
            when(jwtService.generateToken(user)).thenReturn("jwtToken");

            String token = userService.loginUser(loginRequest);

            assertEquals("jwtToken", token);
        }
    }


    @Test
    void loginUser_FailureWhenUserNotFound() {
        UserLoginRequestDTO loginRequest = new UserLoginRequestDTO("nonexistent", "password123");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> userService.loginUser(loginRequest));
    }

    @Test
    void loginUser_FailureWhenPasswordIncorrect() {
        UserLoginRequestDTO loginRequest = new UserLoginRequestDTO("testuser", "wrongPassword");

        User user = User.builder()
                .username("testuser")
                .passwordHash("$2a$10$7q8y9z2yF6E5k6g0sQ0U1eRrGZ3uL.zLkD2nPnI7eN/dM9C6vA1bK")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPasswordHash())).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> userService.loginUser(loginRequest));
    }

    @Test
    void verifyUser_Success() {
        User user = User.builder().enabled(false).build();
        VerificationToken token = new VerificationToken();
        token.setToken("token123");
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));

        when(verificationTokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.verifyUser("token123");

        assertTrue(result.getEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void verifyUser_FailureWhenTokenNotFound() {
        when(verificationTokenRepository.findByToken("badtoken")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> userService.verifyUser("badtoken"));
    }

    @Test
    void verifyUser_FailureWhenTokenExpired() {
        User user = User.builder().enabled(false).build();
        VerificationToken token = new VerificationToken();
        token.setToken("token123");
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));

        when(verificationTokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        assertThrows(IllegalStateException.class, () -> userService.verifyUser("token123"));
    }

    @Test
    void forgotPassword_Success() {
        User user = User.builder().email("test@example.com").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        String resetLink = userService.forgotPassword("test@example.com");

        assertNotNull(resetLink);
        assertTrue(resetLink.contains("token="));
        verify(passwordResetTokenRepository).save(any());
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    void forgotPassword_Failure() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> userService.forgotPassword("missing@example.com"));
    }

    @Test
    void resetPassword_Success() {
        User user = User.builder().build();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("reset123");
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));

        when(passwordResetTokenRepository.findByToken("reset123")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");

        User result = userService.resetPassword("reset123", "newPassword");

        assertEquals("encodedPassword", result.getPasswordHash());
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(token);
    }

    @Test
    void resetPassword_FailureWhenTokenNotFound() {
        when(passwordResetTokenRepository.findByToken("badtoken")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> userService.resetPassword("badtoken", "newPassword"));
    }

    @Test
    void resetPassword_FailureWhenTokenExpired() {
        User user = User.builder().build();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("expiredtoken");
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().minusHours(1));

        when(passwordResetTokenRepository.findByToken("expiredtoken")).thenReturn(Optional.of(token));

        assertThrows(IllegalStateException.class, () -> userService.resetPassword("expiredtoken", "newPassword"));
    }
}
