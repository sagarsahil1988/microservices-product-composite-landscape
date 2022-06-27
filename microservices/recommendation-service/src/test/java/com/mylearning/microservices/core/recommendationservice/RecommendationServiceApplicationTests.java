package com.mylearning.microservices.core.recommendationservice;

import com.mylearning.microservices.api_definition.core.recommendation.Recommendation;
import com.mylearning.microservices.api_definition.event.Event;
import com.mylearning.microservices.core.recommendationservice.persistance.RecommendationRepository;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class RecommendationServiceApplicationTests {

	@Autowired
	RecommendationRepository recommendationRepository;

	@Autowired
	WebTestClient webTestClient;

	@Autowired
	@Qualifier("messageProcessor")
	private Consumer<Event<Integer, Recommendation>> messageProcessor;

	@BeforeEach
	public void setUpDatabase(){
		recommendationRepository.deleteAll().block();
	}

	@Test
	public void getRecommendationByProductId(){
		int productId =1;
		sendCreateRecommendationEvent(productId, 1);
		sendCreateRecommendationEvent(productId, 2);
		sendCreateRecommendationEvent(productId,3);
		Assertions.assertEquals(3, recommendationRepository.findByProductId(productId).count().block());

		getAndVerifyRecommendationsByProductId(productId, OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productId").isEqualTo(productId)
				.jsonPath("$[2].recommendationId").isEqualTo(3);
	}

	private void sendCreateRecommendationEvent(int productId, int recommendationId) {
		Recommendation recommendation = new Recommendation(productId, recommendationId, "Author" + recommendationId,
				recommendationId, "Content" + recommendationId, "SA");

		Event <Integer, Recommendation> event = new Event(Event.Type.CREATE, productId, recommendationId);
		messageProcessor.accept(event);
	}

	@Test
	public void duplicateError(){
		int productId = 1;
		int recommendationId = 1;
		sendCreateRecommendationEvent(productId, recommendationId);

		Assertions.assertEquals(1,recommendationRepository.count().block());
		//ADD CODE FOR EXCEPTION HERE
	}

	@Test
	public void deleteRecommendationsByProductId(){
		int productId=1;
		int recommendationId = 1;

		sendCreateRecommendationEvent(productId, recommendationId);
		Assertions.assertEquals(1, recommendationRepository.findByProductId(productId).count().block());

		sendDeleteRecommendationEvent(productId);
		Assertions.assertEquals(0, recommendationRepository. findByProductId(productId).count().block());
		sendDeleteRecommendationEvent(productId);
	}

	private void sendDeleteRecommendationEvent(int productId) {
		Event<Integer, Recommendation> event = new Event<>(Event.Type.DELETE, productId, null);
		messageProcessor.accept(event);
	}

//	@Test
//	public void getRecommendationMissingParameter(){
//		getAndVerifyRecommendationsByProductId("", BAD_REQUEST)
//				.jsonPath("$.path").isEqualTo("/recommendation");
////				.jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
//	}
//
//	@Test
//	public void getRecommendationInvalidParameter(){
//		getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST)
//				.jsonPath("$.path").isEqualTo("/recommendation");
////				.jsonPath("$.message").isEqualTo("Type mismatch.");
//	}
//
//	@Test
//	public void getRecommendationNotFound(){
//		getAndVerifyRecommendationsByProductId("?productId=113", OK)
//				.jsonPath("$.length()").isEqualTo(0);
//	}
//
//	@Test
//	public void getRecommendationInvalidParameterNegativeValue(){
//		int productInvalidId = -1;
//		getAndVerifyRecommendationsByProductId("?productId="+ productInvalidId, UNPROCESSABLE_ENTITY)
//				.jsonPath("$.path").isEqualTo("/recommendation")
//				.jsonPath("$.message").isEqualTo("Invalid productId: "+productInvalidId);
//
//	}
//
//	private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus){
//		return webTestClient.delete()
//				.uri("/recommendation?productId="+ productId)
//				.accept(MediaType.APPLICATION_JSON)
//				.exchange()
//				.expectStatus().isEqualTo(expectedStatus)
//				.expectBody();
//	}
//
	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {

		return getAndVerifyRecommendationsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus){

		return webTestClient.get()
				.uri("/recommendation" + productIdQuery)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();

	}
//
//	private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recommendationId, HttpStatus expectedStatus) {
//		Recommendation recommendation = new Recommendation(productId, recommendationId, "Author" + recommendationId, recommendationId, "Content" + recommendationId, "SA");
//
//		return webTestClient.post()
//				.uri("/recommendation")
//				.body(reactor.core.publisher.Mono.just(recommendation), Recommendation.class)
//				.accept(MediaType.APPLICATION_JSON)
//				.exchange()
//				.expectStatus().isEqualTo(expectedStatus)
//				.expectHeader().contentType(MediaType.APPLICATION_JSON)
//				.expectBody();
//	}

}
