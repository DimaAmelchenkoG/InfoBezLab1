package ru.itmo.is.lab1.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ru.itmo.is.lab1.domain.Announcement;
import ru.itmo.is.lab1.domain.UserAccount;
import ru.itmo.is.lab1.repository.AnnouncementRepository;
import ru.itmo.is.lab1.repository.UserAccountRepository;

@Component
public class DataInitializer implements ApplicationRunner {

	private final UserAccountRepository userAccountRepository;
	private final AnnouncementRepository announcementRepository;
	private final PasswordEncoder passwordEncoder;
	private final AppProperties properties;

	public DataInitializer(
			UserAccountRepository userAccountRepository,
			AnnouncementRepository announcementRepository,
			PasswordEncoder passwordEncoder,
			AppProperties properties) {
		this.userAccountRepository = userAccountRepository;
		this.announcementRepository = announcementRepository;
		this.passwordEncoder = passwordEncoder;
		this.properties = properties;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (userAccountRepository.count() == 0) {
			if (properties.getDemoUsers().isEmpty()) {
				throw new IllegalStateException("Demo users are not configured");
			}
			for (AppProperties.DemoUser demoUser : properties.getDemoUsers()) {
				UserAccount account = new UserAccount();
				account.setUsername(demoUser.getUsername());
				account.setPasswordHash(passwordEncoder.encode(demoUser.getPassword()));
				account.setDisplayName(demoUser.getDisplayName());
				userAccountRepository.save(account);
			}
		}
		if (announcementRepository.count() == 0) {
			saveAnnouncement(
					"Политика паролей",
					"Пароли хранятся только в виде хэша bcrypt. Повторное использование пароля запрещено.");
			saveAnnouncement(
					"Проверка зависимостей",
					"Перед выпуском версии запускается OWASP Dependency-Check и ищутся известные CVE.");
			saveAnnouncement(
					"Журнал аудита",
					"Доступ к данным API разрешён только после проверки JWT-токена.");
		}
	}

	private void saveAnnouncement(String title, String body) {
		Announcement announcement = new Announcement();
		announcement.setTitle(title);
		announcement.setBody(body);
		announcementRepository.save(announcement);
	}

}
