package ru.itmo.is.lab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SecureRestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecureRestApiApplication.class, args);
	}

}
