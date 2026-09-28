package ru.itmo.is.lab1.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import ru.itmo.is.lab1.service.NoteService;
import ru.itmo.is.lab1.web.dto.NoteRequest;
import ru.itmo.is.lab1.web.dto.NoteResponse;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

	private final NoteService noteService;

	public NoteController(NoteService noteService) {
		this.noteService = noteService;
	}

	@GetMapping
	public List<NoteResponse> list() {
		return noteService.listMine();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public NoteResponse create(@Valid @RequestBody NoteRequest request) {
		return noteService.create(request);
	}

}
