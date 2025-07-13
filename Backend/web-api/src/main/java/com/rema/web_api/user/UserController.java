package com.rema.web_api.user;

import com.rema.web_api.user.dto.UserDTO;
import com.rema.web_api.user.dto.UserRegistrationRequestDTO;
import com.rema.web_api.user.User;
import com.rema.web_api.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService _userService)
    {
        this.userService = _userService;
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
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegistrationRequestDTO registerUserDTO)
    {
        try
        {
            User userOptional = userService.registerUser(registerUserDTO);
            return ResponseEntity.ok().build();
        }
        catch (IllegalStateException e)
        {
            return ResponseEntity.
                    badRequest()
                    .body(e.getMessage());
        }
    }


}
