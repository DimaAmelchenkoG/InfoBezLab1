package ru.itmo.is.lab1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.itmo.is.lab1.domain.Note;

public interface NoteRepository extends JpaRepository<Note, Long> {

	List<Note> findByAuthorUsernameOrderByCreatedAtDesc(String authorUsername);

}
