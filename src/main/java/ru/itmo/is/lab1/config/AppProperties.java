package ru.itmo.is.lab1.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private Jwt jwt = new Jwt();
	private List<DemoUser> demoUsers = new ArrayList<>();

	public Jwt getJwt() {
		return jwt;
	}

	public void setJwt(Jwt jwt) {
		this.jwt = jwt;
	}

	public List<DemoUser> getDemoUsers() {
		return demoUsers;
	}

	public void setDemoUsers(List<DemoUser> demoUsers) {
		this.demoUsers = demoUsers;
	}

	public static class Jwt {

		private String secret;
		private Duration ttl = Duration.ofHours(1);

		public String getSecret() {
			return secret;
		}

		public void setSecret(String secret) {
			this.secret = secret;
		}

		public Duration getTtl() {
			return ttl;
		}

		public void setTtl(Duration ttl) {
			this.ttl = ttl;
		}

	}

	public static class DemoUser {

		private String username;
		private String password;
		private String displayName;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

	}

}
