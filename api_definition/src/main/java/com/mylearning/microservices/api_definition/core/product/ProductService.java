package com.mylearning.microservices.api_definition.core.product;


import org.springframework.web.bind.annotation.*;

public interface ProductService {

    @PostMapping(
            value = "/product",
            consumes = "application/json",
            produces = "application/json"
    )
    Product createProduct(@RequestBody(required = true) Product body);

    @GetMapping(
            value = "/product/{productId}",
            produces = "application/json"
    )
    Product getProduct(@PathVariable int productId);

    @DeleteMapping(
            value = "/product/{productId}"
    )
    void deleteProduct(@PathVariable int productId);

}

