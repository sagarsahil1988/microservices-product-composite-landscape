package com.mylearning.microservices.core.productservice;

import org.mapstruct.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ComponentScan("com.mylearning")
public class ProductServiceApplication {
	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceApplication.class);


	public static void main(String[] args) {

		ConfigurableApplicationContext configurableApplicationContext =
		SpringApplication.run(ProductServiceApplication.class, args);

		String mongoDbHost = configurableApplicationContext.getEnvironment().getProperty("spring.data.mongodb.host");
		String mongoDbPort = configurableApplicationContext.getEnvironment().getProperty("spring.data.mongodb.port");

		LOG.info("Connected to MongoDB : " + mongoDbHost + ":" + mongoDbPort);
	}

}
