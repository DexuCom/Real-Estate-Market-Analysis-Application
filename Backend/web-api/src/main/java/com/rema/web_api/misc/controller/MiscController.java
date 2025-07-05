package com.rema.web_api.misc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MiscController {
    @GetMapping("/ping")
    public String ping()
    {
        return "Pong, API running successfully";
    }
}
