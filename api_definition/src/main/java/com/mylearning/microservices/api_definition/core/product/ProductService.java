package com.mylearning.microservices.api_definition.core.product;


import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ProductService {

    @PostMapping(
            value = "/product",
            consumes = "application/json",
            produces = "application/json"
    )
    Mono<Product> createProduct(Product body);

    @GetMapping(
            value = "/product/{productId}",
            produces = "application/json"
    )
    Mono<Product> getProduct(@PathVariable int productId);

    @DeleteMapping(
            value = "/product/{productId}"
    )
    Mono<Void> deleteProduct(int productId);

}

