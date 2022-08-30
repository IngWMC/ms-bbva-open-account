package com.nttdata.bbva.openaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class OpenAccountApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenAccountApplication.class, args);
	}

}
