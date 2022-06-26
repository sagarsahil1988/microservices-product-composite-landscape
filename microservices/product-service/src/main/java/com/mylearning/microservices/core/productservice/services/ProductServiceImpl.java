package com.mylearning.microservices.core.productservice.services;


import com.mylearning.microservices.api_definition.core.product.Product;
import com.mylearning.microservices.api_definition.core.product.ProductService;
import com.mylearning.microservices.core.productservice.persistance.ProductEntity;
import com.mylearning.microservices.core.productservice.persistance.ProductRepository;
import com.mylearning.microservices.core.utility.exceptions.InvalidInputEception;
import com.mylearning.microservices.core.utility.exceptions.NotFoundException;
import com.mylearning.microservices.core.utility.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

@Autowired
    private final ProductRepository productRepository;
    private final ProductMapper mapper;
    private final ServiceUtil serviceUtil;


    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductMapper mapper, ServiceUtil serviceUtil){
        this.productRepository = productRepository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;

    }

    @Override
    public Mono<Product> createProduct(Product body) {

        if(body.getProductId() < 1){
            throw new InvalidInputEception("Invalid Product Id:- " + body.getProductId());
        }

        ProductEntity entity = mapper.apiToEntity(body);
        Mono<Product> newEntity= productRepository.save(entity)
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputEception("Duplicate Key, Product Id:- " + body.getProductId()))
                .map(e -> mapper.entityToApi(e));

        return newEntity;
    }

    @Override
    public Mono<Product> getProduct(int productId) {

        if(productId < 1){
            throw new InvalidInputEception("Invalid Product Id:- " + productId);
        }

        LOG.info("Retrieving product information for id:- {} ", productId);

        return productRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId:- " + productId)))
                .log(LOG.getName(),Level.FINE)
                .map(mapper::entityToApi)
                .map(this::setServiceAddress);

    }

    private Product setServiceAddress(Product product) {
        product.setServiceAddress(serviceUtil.getServiceAddress());
        return product;
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        if( productId < 1)
        {
            throw new InvalidInputEception("Invalid Product Id:- " + productId);
        }
        LOG.info("delete product: tries to delete and entity with productId:- {}", productId);
        return productRepository.findByProductId(productId)
                .log(LOG.getName(),Level.FINE)
                .map(e -> productRepository.delete(e))
                .flatMap(e -> e);
    }
}
