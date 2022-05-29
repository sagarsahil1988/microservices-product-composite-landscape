package com.mylearning.microservices.core.productcompositeservice;

import com.mylearning.microservices.api_definition.composite.product.ProductAggregate;
import com.mylearning.microservices.api_definition.composite.product.RecommendationSummary;
import com.mylearning.microservices.api_definition.composite.product.ReviewSummary;
import com.mylearning.microservices.api_definition.core.product.Product;
import com.mylearning.microservices.api_definition.core.recommendation.Recommendation;
import com.mylearning.microservices.api_definition.core.review.Review;
import com.mylearning.microservices.core.productcompositeservice.services.ProductCompositeIntegration;
import com.mylearning.microservices.core.utility.exceptions.InvalidInputEception;
import com.mylearning.microservices.core.utility.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ProductCompositeServiceApplicationTests {

	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private ProductCompositeIntegration productCompositeIntegration;

	@BeforeEach
	public void setUp(){
		Mockito.when(productCompositeIntegration.getProduct(PRODUCT_ID_OK))
				.thenReturn(new Product(PRODUCT_ID_OK, "name", 1 , "mock-address"));
		Mockito.when(productCompositeIntegration.getRecommendations(PRODUCT_ID_OK))
				.thenReturn(Collections.singletonList(new Recommendation(PRODUCT_ID_OK, 1 , "author", 1, "Content", "mock address")));
		Mockito.when(productCompositeIntegration.getReviews(PRODUCT_ID_OK))
				.thenReturn(Collections.singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address")));

		Mockito.when(productCompositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT FOUND: "+ PRODUCT_ID_NOT_FOUND));

		Mockito.when(productCompositeIntegration.getProduct(PRODUCT_ID_INVALID))
				.thenThrow(new InvalidInputEception("INVALID: " + PRODUCT_ID_INVALID));
	}

	@Test
	public void contextLoad(){

	}

	@Test
	public void createCompositeProduct1(){
		ProductAggregate productAggregate = new ProductAggregate(1, "name", 1, null, null, null);
		postAndVerifyProduct(productAggregate, HttpStatus.OK);
	}

	@Test
	public void createCompositeProduct2(){
		ProductAggregate productAggregate = new ProductAggregate(1, "name", 1,
				Collections.singletonList(new ReviewSummary(1, "a", "s", "c")),
				Collections.singletonList(new RecommendationSummary(1, "a", 1, "c")), null);
		postAndVerifyProduct(productAggregate, HttpStatus.OK);
	}

	@Test
	public void deleteCompositeProduct(){
		ProductAggregate productAggregate = new ProductAggregate(1, "name", 1,
				Collections.singletonList(new ReviewSummary(1, "a", "s", "c")),
				Collections.singletonList(new RecommendationSummary(1, "a", 1, "c")), null);
		postAndVerifyProduct(productAggregate, HttpStatus.OK);

		deleteAndVerifyuProduct(productAggregate.getProductId(), HttpStatus.OK);
		deleteAndVerifyuProduct(productAggregate.getProductId(), HttpStatus.OK);
	}

	@Test
	public void getProductById(){
		getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
				.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$.recommendations.length()").isEqualTo(1)
				.jsonPath("$.reviews.length()").isEqualTo(1);
	}

	@Test
	public void getProductNotFound(){
		getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("NOT FOUND: "+ PRODUCT_ID_NOT_FOUND);
	}

	@Test
	public void getProductInvalidInput(){
		getAndVerifyProduct(PRODUCT_ID_INVALID, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
				.jsonPath("$.message").isEqualTo("INVALID: "+ PRODUCT_ID_INVALID);
	}
	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return webTestClient.get()
				.uri("/product-composite/" + productId)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

	private void deleteAndVerifyuProduct(int productId, HttpStatus expectedStatus) {
		webTestClient.delete()
				.uri("/product-composite/" + productId)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus);
	}


	private void postAndVerifyProduct(ProductAggregate productAggregate, HttpStatus expectedStatus) {
		webTestClient.post()
				.uri("/product-composite")
				.body(Mono.just(productAggregate), ProductAggregate.class)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus);
	}


}
