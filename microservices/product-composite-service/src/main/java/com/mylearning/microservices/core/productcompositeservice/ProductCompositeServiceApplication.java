package com.mylearning.microservices.core.productcompositeservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;

import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;

@SpringBootApplication
@ComponentScan("com.mylearning")
//@EnableSwagger2               //Strting point fot initiating Spring Fox. NOT NEEDED WITH VERSION 3.0.0
public class ProductCompositeServiceApplication {
	@Value("${api.common.title}") String apiTitle;
	@Value("${api.common.description}") String apiDescription;
	@Value("${api.common.version}") String apiVersion;
	@Value("${api.common.termsofserviceurl}") String apiTermsOfServiceUrl;
	@Value("${api.common.contact.name}") String apiContactName;
	@Value("${api.common.contact.url}") String apiContactUrl;
	@Value("${api.common.contact.email}") String apiContactEmail;
	@Value("${api.common.license}") String apiLicense;
	@Value("${api.common.license.url}") String apiLicenseUrl;



	@Bean
	RestTemplate restTemplate(){
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}

	@Bean		//Docket bean to create Swagger V2 documentation.
	public Docket apiDcumentation(){
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(basePackage("com.mylearning.microservices.core.productcompositeservice.services"))
				.paths(PathSelectors.any())
				.build()
				.globalResponses(HttpMethod.GET, Collections.emptyList())
				.apiInfo(new ApiInfo(
						apiTitle,
						apiDescription,
						apiVersion,
						apiTermsOfServiceUrl,
						new Contact(apiContactName, apiContactUrl, apiContactEmail),
						apiLicense,
						apiLicenseUrl,
						Collections.emptyList()
				));
	}

}
