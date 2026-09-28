package ru.itmo.is.lab1.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
		@NotBlank @Size(max = 64) String username,
		@NotBlank @Size(max = 128) String password) {
}
