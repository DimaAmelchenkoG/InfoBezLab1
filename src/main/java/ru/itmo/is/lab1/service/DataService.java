package ru.itmo.is.lab1.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.itmo.is.lab1.repository.AnnouncementRepository;
import ru.itmo.is.lab1.web.dto.AnnouncementResponse;

@Service
public class DataService {

	private final AnnouncementRepository announcementRepository;

	public DataService(AnnouncementRepository announcementRepository) {
		this.announcementRepository = announcementRepository;
	}

	@Transactional(readOnly = true)
	public List<AnnouncementResponse> find(String query) {
		var announcements = (query == null || query.isBlank())
				? announcementRepository.findAllByOrderByIdAsc()
				: announcementRepository.searchByQuery(query.trim());
		return announcements.stream().map(AnnouncementResponse::from).toList();
	}

}
