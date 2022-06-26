package com.mylearning.microservices.api_definition.core.review;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReviewService {

    @PostMapping(
            value = "/review",
            consumes = "application/json",
            produces = "application/json"
    )
    Mono<Review> createReview(Review body);

    @GetMapping(
            value    = "/review",
            produces = "application/json")
    Flux<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);

    @DeleteMapping(
            value = "/review"
    )
    Mono<Void> deleteReviews(int productId);
}
