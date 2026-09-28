package ru.itmo.is.lab1.web.dto;

import ru.itmo.is.lab1.domain.Announcement;
import ru.itmo.is.lab1.util.XssSanitizer;

public record AnnouncementResponse(Long id, String title, String body) {

	public static AnnouncementResponse from(Announcement announcement) {
		return new AnnouncementResponse(
				announcement.getId(),
				XssSanitizer.escape(announcement.getTitle()),
				XssSanitizer.escape(announcement.getBody()));
	}

}
