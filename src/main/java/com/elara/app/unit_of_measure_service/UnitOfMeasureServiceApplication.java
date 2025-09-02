package com.elara.app.unit_of_measure_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class UnitOfMeasureServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnitOfMeasureServiceApplication.class, args);
	}

}
