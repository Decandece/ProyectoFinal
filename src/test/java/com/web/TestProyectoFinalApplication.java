package com.web;

import org.springframework.boot.SpringApplication;

public class TestProyectoFinalApplication {

	public static void main(String[] args) {
		SpringApplication.from(ProyectoFinalApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
