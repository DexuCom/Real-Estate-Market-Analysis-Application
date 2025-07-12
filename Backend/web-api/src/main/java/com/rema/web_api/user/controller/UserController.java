package com.rema.web_api.user.controller;

import com.rema.web_api.user.dto.UserRegistrationRequestDTO;
import com.rema.web_api.user.model.User;
import com.rema.web_api.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService _userService)
    {
        this.userService = _userService;
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
