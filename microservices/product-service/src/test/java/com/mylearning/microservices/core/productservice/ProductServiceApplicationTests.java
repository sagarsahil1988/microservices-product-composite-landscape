package com.mylearning.microservices.core.productservice;

import com.mylearning.microservices.api_definition.core.product.Product;
import com.mylearning.microservices.api_definition.event.Event;
import com.mylearning.microservices.core.productservice.persistance.ProductRepository;
import com.mylearning.microservices.core.utility.exceptions.InvalidInputEception;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

import static org.springframework.http.HttpStatus.*;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class ProductServiceApplicationTests {

	@Autowired
	ProductRepository productRepository;

	@Autowired
	WebTestClient webTestClient;

	@Autowired
	@Qualifier("messageProcessor")
	private Consumer<Event<Integer, Product>> messageProcessor;

	@BeforeEach
	public void setUpDatabase(){
		productRepository.deleteAll().block();
	}

	@Test
	public void getProductById(){
		int productId = 1;

		Assertions.assertNull(productRepository.findByProductId(productId).block());
		Assertions.assertEquals(0, productRepository.count().block());

		sendCreateProductEvent(productId);

		postAndVerifyProduct(productId, OK);

		Assertions.assertNotNull(productRepository.findByProductId(productId).block());
		Assertions.assertEquals(0, productRepository.count().block());

		getAndVerifyProduct(productId, OK)
				.jsonPath("$.productId").isEqualTo(productId);
	}

	private void sendCreateProductEvent(int productId) {
		Product product = new Product(productId, "Name"+productId , productId, "SA");
		Event<Integer, Product> event = new Event(Event.Type.CREATE, productId, product);
		messageProcessor.accept(event);

	}

	@Test
	public void duplicateError(){
		int productId = 1;

		Assertions.assertNull(productRepository.findByProductId(productId).block());
		sendCreateProductEvent(productId);
		Assertions.assertNotNull(productRepository.findByProductId(productId).block());

		Assertions.assertThrows(InvalidInputEception.class, () -> sendCreateProductEvent(productId), "Expected a InvalidInputException here!");

	}

	@Test
	public void deleteProduct(){
		int productId = 1;
		sendCreateProductEvent(productId);
		Assertions.assertNotNull(productRepository.findByProductId(productId).block());
		sendDeleteProductEvent(productId);
		Assertions.assertNull(productRepository.findByProductId(productId).block());

		sendDeleteProductEvent(productId);

	}

	private void sendDeleteProductEvent(int productId) {
		Event<Integer, Product> event = new Event<>(Event.Type.DELETE, productId, null);
		messageProcessor.accept(event);
	}

	@Test
	public void getProductInvalidParameterString(){

		getAndVerifyProduct("/no-integer", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/product/no-integer")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
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
