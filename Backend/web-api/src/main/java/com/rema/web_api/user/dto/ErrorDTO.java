package com.rema.web_api.user.dto;

public record ErrorDTO (
        String message,
        int status
){ }
