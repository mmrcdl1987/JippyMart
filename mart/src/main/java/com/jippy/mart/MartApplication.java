package com.jippy.mart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MartApplication {

	public static void main(String[] args) {
		SpringApplication.run(MartApplication.class, args);
	}

}
