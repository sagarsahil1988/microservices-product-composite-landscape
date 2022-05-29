package com.mylearning.microservices.core.recommendationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan("com.mylearning")
public class RecommendationServiceApplication {

	private final static Logger LOG = LoggerFactory.getLogger(RecommendationServiceApplication.class);
	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplicationContext =
		SpringApplication.run(RecommendationServiceApplication.class, args);

		String mongoDbHost = configurableApplicationContext.getEnvironment().getProperty("spring.data.mongodb.host");
		String mongoDbPort = configurableApplicationContext.getEnvironment().getProperty("spring.data.mongodb.port");

		LOG.info("Connected to MongoDB : " + mongoDbHost + ":" + mongoDbPort);

	}

}
