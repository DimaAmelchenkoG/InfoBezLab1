package ru.itmo.is.lab1.web.dto;

public record LoginResponse(String token, String tokenType, long expiresInSeconds, String username) {
}
