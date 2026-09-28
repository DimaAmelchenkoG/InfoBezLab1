package ru.itmo.is.lab1.web.dto;

import java.time.Instant;

import ru.itmo.is.lab1.domain.Note;
import ru.itmo.is.lab1.util.XssSanitizer;

public record NoteResponse(Long id, String title, String content, String author, Instant createdAt) {

	public static NoteResponse from(Note note) {
		return new NoteResponse(
				note.getId(),
				XssSanitizer.escape(note.getTitle()),
				XssSanitizer.escape(note.getContent()),
				XssSanitizer.escape(note.getAuthorUsername()),
				note.getCreatedAt());
	}

}
