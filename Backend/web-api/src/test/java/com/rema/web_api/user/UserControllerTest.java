package com.rema.web_api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rema.web_api.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@EnableWebMvc
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UUID userId;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(Role.USER)
                .enabled(true)
                .build();

    }

    @Test
    void getUserData_Success() throws Exception {
        when(userService.getUserById(userId)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUserData_NotFound() throws Exception {
        when(userService.getUserById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerUser_Success() throws Exception {
        when(userService.registerUser(any())).thenReturn(testUser);

        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testuser");
        payload.put("email", "test@example.com");
        payload.put("password", "password123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(userService).registerUser(any());
    }

    @Test
    void registerUser_UserExists() throws Exception {
        when(userService.registerUser(any()))
                .thenThrow(new IllegalStateException("Użytkownik o takiej nazwie już istnieje!"));

        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testuser");
        payload.put("email", "test@example.com");
        payload.put("password", "password123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Użytkownik o takiej nazwie już istnieje!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void loginUser_Success() throws Exception {
        String jwtToken = "testJwtToken";
        when(userService.loginUser(any())).thenReturn(jwtToken);

        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testuser");
        payload.put("password", "password123");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value(jwtToken));
    }

    @Test
    void loginUser_InvalidCredentials() throws Exception {
        when(userService.loginUser(any()))
                .thenThrow(new IllegalStateException("Niepoprawne haslo"));

        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testuser");
        payload.put("password", "wrongpassword");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Niepoprawne haslo"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void verifyUser_Success() throws Exception {
        when(userService.verifyUser("valid-token")).thenReturn(testUser);

        mockMvc.perform(get("/api/users/verify")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("\"Konto zostało pomyślnie zweryfikowane!\""));
    }

    @Test
    void verifyUser_InvalidToken() throws Exception {
        when(userService.verifyUser("invalidToken"))
                .thenThrow(new IllegalArgumentException("Nieprawidłowy token weryfikacyjny!"));

        mockMvc.perform(get("/api/users/verify")
                        .param("token", "invalidToken"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nieprawidłowy token weryfikacyjny!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void forgotPassword_Success() throws Exception {
        when(userService.forgotPassword("test@example.com"))
                .thenReturn("reset-link");

        mockMvc.perform(post("/api/users/forgot-password")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().json("\"Link do resetowania hasła został wysłany na podany adres email.\""));
    }

    @Test
    void forgotPassword_UserNotFound() throws Exception {
        when(userService.forgotPassword("invalid@example.com"))
                .thenThrow(new IllegalStateException("Nie znaleziono użytkownika z podanym adresem email!"));

        mockMvc.perform(post("/api/users/forgot-password")
                        .param("email", "invalid@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nie znaleziono użytkownika z podanym adresem email!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void resetPassword_Success() throws Exception {
        when(userService.resetPassword("validToken", "newPassword123")).thenReturn(testUser);

        mockMvc.perform(post("/api/users/reset-password")
                        .param("token", "validToken")
                        .param("newPassword", "newPassword123"))
                .andExpect(status().isOk())
                .andExpect(content().string("\"Hasło zostało pomyślnie zresetowane!\""));
    }

    @Test
    void resetPassword_InvalidToken() throws Exception {
        when(userService.resetPassword("invalidToken", "newPassword123"))
                .thenThrow(new IllegalArgumentException("Nieprawidłowy token do resetowania hasła!"));

        mockMvc.perform(post("/api/users/reset-password")
                        .param("token", "invalidToken")
                        .param("newPassword", "newPassword123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nieprawidłowy token do resetowania hasła!"))
                .andExpect(jsonPath("$.status").value(400));
    }
}