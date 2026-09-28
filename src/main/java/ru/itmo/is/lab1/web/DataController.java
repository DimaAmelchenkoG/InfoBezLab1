package ru.itmo.is.lab1.web;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Size;

import ru.itmo.is.lab1.service.DataService;
import ru.itmo.is.lab1.web.dto.AnnouncementResponse;

@RestController
@RequestMapping("/api/data")
@Validated
public class DataController {

	private final DataService dataService;

	public DataController(DataService dataService) {
		this.dataService = dataService;
	}

	@GetMapping
	public List<AnnouncementResponse> list(
			@RequestParam(name = "q", required = false) @Size(max = 100) String q) {
		return dataService.find(q);
	}

}
