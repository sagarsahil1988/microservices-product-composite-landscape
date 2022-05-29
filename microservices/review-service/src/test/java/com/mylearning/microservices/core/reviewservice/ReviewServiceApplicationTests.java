package com.mylearning.microservices.core.reviewservice;

import com.mylearning.microservices.api_definition.core.review.Review;
import com.mylearning.microservices.core.reviewservice.persistance.ReviewRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import javax.swing.text.html.HTML;

@SpringBootTest
		(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {"spring.datasource.url=jdbc:h2:mem:review-db"}
		)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ReviewServiceApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ReviewRepository reviewRepository;

	@BeforeEach
	public void setUpDatabase(){
		reviewRepository.deleteAll();
	}

	@Test
	public void getReviewsByProductId(){
		int productId = 1;
		Assertions.assertEquals(0, reviewRepository.findByProductId(productId).size());
		postAndVerifyReview(productId, 1, HttpStatus.OK);
		postAndVerifyReview(productId, 2, HttpStatus.OK);
		postAndVerifyReview(productId, 3, HttpStatus.OK);
		Assertions.assertEquals(3, reviewRepository.findByProductId(productId).size());

		getAndVerifyReviewsByProductId(productId, HttpStatus.OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$.[2].productId").isEqualTo(productId)
				.jsonPath("$.[2].reviewId").isEqualTo(3);

	}

	@Test
	public void duplicateError(){
		int productId = 1;
		int reviewId = 1;
		Assertions.assertEquals(0, reviewRepository.count());
		postAndVerifyReview(productId, reviewId, HttpStatus.OK)
				.jsonPath("$.productId").isEqualTo(productId)
				.jsonPath("$.reviewId").isEqualTo(reviewId);

		Assertions.assertEquals(1, reviewRepository.count());
		postAndVerifyReview(productId, reviewId, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id: 1");
	}

	@Test
	public void deleteReviews(){
		int productId = 1;
		int reviewId = 1;

		postAndVerifyReview(productId, reviewId, HttpStatus.OK);
		Assertions.assertEquals(1, reviewRepository.findByProductId(productId).size());
		deleteAndVerifyReviewsByProductId(productId, HttpStatus.OK);
	}

	@Test
	public void getReviewsInvalidParameterNegativeValue() {

		int productIdInvalid = -1;

		getAndVerifyReviewsByProductId("?productId=" + productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}


	private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
		return webTestClient.delete()
				.uri("/review?productId="+ productId)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
		return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus){
		return webTestClient.get()
				.uri("/review"+productIdQuery)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int reviewId, HttpStatus expectedStatus) {
		Review review = new Review(productId, reviewId, "Author" + reviewId, "Subjec" + reviewId, "Content" + reviewId, "SA");

		return webTestClient.post()
				.uri("/review")
				.body(Mono.just(review), Review.class)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}
}
