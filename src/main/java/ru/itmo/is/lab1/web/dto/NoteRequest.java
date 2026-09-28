package ru.itmo.is.lab1.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteRequest(
		@NotBlank @Size(max = 120) String title,
		@NotBlank @Size(max = 2000) String content) {
}
