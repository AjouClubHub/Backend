package com.coldrice.clubing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ClubingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClubingApplication.class, args);
	}

}
