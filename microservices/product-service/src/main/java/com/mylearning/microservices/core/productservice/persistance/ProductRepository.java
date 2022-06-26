package com.mylearning.microservices.core.productservice.persistance;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ProductRepository extends ReactiveCrudRepository<ProductEntity, String> {

        Mono<ProductEntity> findByProductId(int productId);
}
