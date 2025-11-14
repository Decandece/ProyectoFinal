package com.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProyectoFinalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectoFinalApplication.class, args);
	}

}
