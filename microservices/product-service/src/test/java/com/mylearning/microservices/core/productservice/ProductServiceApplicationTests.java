package com.mylearning.microservices.core.productservice;

import com.mylearning.microservices.api_definition.core.product.Product;
import com.mylearning.microservices.core.productservice.persistance.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.HttpStatus.*;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class ProductServiceApplicationTests {

	@Autowired
	ProductRepository productRepository;

	@Autowired
	WebTestClient webTestClient;

	@BeforeEach
	public void setUpDatabase(){
		productRepository.deleteAll();
	}

	@Test
	public void getProductById(){
		int productId =1;
		postAndVerifyProduct(productId, OK);
		Assertions.assertTrue(productRepository.findByProductId(productId).isPresent());
		getAndVerifyProduct(productId, OK).jsonPath("$.productId").isEqualTo(productId);
	}



	@Test
	public void duplicateError(){
		int productId =1;
		postAndVerifyProduct(productId, OK);
		Assertions.assertTrue(productRepository.findByProductId(productId).isPresent());

		postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product")
				.jsonPath("$.message").isEqualTo("Duplicate Key , Product Id : " + productId);
	}

	@Test
	public void deleteProduct(){
		int productId = 1;
		postAndVerifyProduct(productId,OK);
		deleteAndVerifyProduct(productId, OK);
		Assertions.assertFalse(productRepository.findByProductId(productId).isPresent());

	}

	@Test
	public void getProductInvalidParameterString(){
		System.out.println(getAndVerifyProduct("/no-integer", BAD_REQUEST).toString());
		getAndVerifyProduct("/no-integer", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/product/no-integer");
//				.jsonPath("$.message").isEqualTo("Type mismatch");
	}

	@Test
	public void getProductNotFound(){
		int productIdNotFound = 13;
		getAndVerifyProduct(productIdNotFound, NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/product/"+productIdNotFound)
				.jsonPath("$.message").isEqualTo("No Product found for productId: " + productIdNotFound);
	}
	private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return webTestClient.delete()
				.uri("/product" +"/"+productId)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}

	@Test
	public void getProductInvalidParameterNegativeValue(){
		int productIdInvalid = -1;
		getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product/"+ productIdInvalid)
				.jsonPath("$.message").isEqualTo("Invalid ProductId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return getAndVerifyProduct("/" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus){
		return webTestClient.get()
				.uri("/product" + productIdPath)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		Product product = new Product(productId, "Name" + productId, productId, "SA");

		return webTestClient.post().uri("/product")
				.body(just(product), Product.class)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}
}
