package com.SSarkar.Xplore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class XploreApplication {

	public static void main(String[] args) {
		SpringApplication.run(XploreApplication.class, args);
	}

}