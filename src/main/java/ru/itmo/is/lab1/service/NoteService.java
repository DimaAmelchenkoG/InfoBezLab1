package ru.itmo.is.lab1.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.itmo.is.lab1.domain.Note;
import ru.itmo.is.lab1.repository.NoteRepository;
import ru.itmo.is.lab1.web.dto.NoteRequest;
import ru.itmo.is.lab1.web.dto.NoteResponse;

@Service
public class NoteService {

	private final NoteRepository noteRepository;

	public NoteService(NoteRepository noteRepository) {
		this.noteRepository = noteRepository;
	}

	@Transactional
	public NoteResponse create(NoteRequest request) {
		Note note = new Note();
		note.setTitle(request.title().trim());
		note.setContent(request.content().trim());
		note.setAuthorUsername(currentUsername());
		note.setCreatedAt(Instant.now());
		return NoteResponse.from(noteRepository.save(note));
	}

	@Transactional(readOnly = true)
	public List<NoteResponse> listMine() {
		return noteRepository.findByAuthorUsernameOrderByCreatedAtDesc(currentUsername()).stream()
				.map(NoteResponse::from)
				.toList();
	}

	private static String currentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
			throw new IllegalStateException("Authenticated user is required");
		}
		return authentication.getName();
	}

}
