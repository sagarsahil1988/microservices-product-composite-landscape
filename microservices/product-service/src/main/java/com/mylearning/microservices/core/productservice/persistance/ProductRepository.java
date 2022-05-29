package com.mylearning.microservices.core.productservice.persistance;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface ProductRepository extends PagingAndSortingRepository<ProductEntity, String> {

        Optional<ProductEntity> findByProductId(int productId);
}
