package com.rema.web_api.global.dto;

public record ErrorDTO (
        String message,
        int status
){ }
