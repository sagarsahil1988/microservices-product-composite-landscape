package com.mylearning.microservices.core.productservice.persistance_tests;


import com.mylearning.microservices.core.productservice.persistance.ProductEntity;
import com.mylearning.microservices.core.productservice.persistance.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.mapping.TextScore;
import reactor.test.StepVerifier;

import javax.annotation.security.RunAs;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.IntStream.rangeClosed;


@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PersistanceTests {

    @Autowired
    private ProductRepository productRepository;

    private ProductEntity savedEntity;

    @BeforeEach
    public void setUpDatabase(){
        StepVerifier.create(productRepository.deleteAll()).verifyComplete();

        ProductEntity productEntity = new ProductEntity(1,"testProductName1",1);

        StepVerifier.create(productRepository.save(productEntity))
                .expectNextMatches(createdEntity -> {
                    savedEntity = createdEntity;
                    return areProductEqual(productEntity, savedEntity);
                })
                .verifyComplete();

    }

    private boolean areProductEqual(ProductEntity expectedEntity, ProductEntity actualEntity) {
        return
                (expectedEntity.getId().equals(actualEntity.getId()))
                        && (expectedEntity.getVersion() == actualEntity.getVersion())
                        && (expectedEntity.getProductId() == actualEntity.getProductId())
                        && (expectedEntity.getName().equals(actualEntity.getName()))
                        && (expectedEntity.getWeight() == actualEntity.getWeight());
    }

    @Test
    public void create(){
        ProductEntity newEntity = new ProductEntity(2,"testProductName2",2);
        StepVerifier.create(productRepository.save(newEntity))
                .expectNextMatches(createdEntity -> newEntity.getProductId() == createdEntity.getProductId())
                .verifyComplete();

        StepVerifier.create(productRepository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity -> areProductEqual(newEntity, foundEntity))
                .verifyComplete();

        StepVerifier.create(productRepository.count())
                .expectNext(2L).verifyComplete();


    }

    @Test
    public void update(){
        savedEntity.setName("updated_name_for_saved_entity");

        StepVerifier.create(productRepository.save(savedEntity))
                .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("updated_name_for_saved_entity"))
                .verifyComplete();

        StepVerifier.create(productRepository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1 && foundEntity.getName().equals("updated_name_for_saved_entity"))
                .verifyComplete();

    }

    @Test
    public void delete(){
        StepVerifier.create(productRepository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(productRepository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
    }

    @Test
    public void getByProductId(){
        StepVerifier.create(productRepository.findByProductId(savedEntity.getProductId()))
                .expectNextMatches(foundEntity -> areProductEqual(savedEntity, foundEntity))
                .verifyComplete();
    }

    @Test
    public void duplicateError(){
            ProductEntity entity = new ProductEntity(savedEntity.getProductId(),"name", 10);
            StepVerifier.create(productRepository.save(entity)).expectError(DuplicateKeyException.class).verify();
    }

    @Test
    public void optimisticLockError(){
        //Store the Entity into two seperate Entity Object
        ProductEntity entity1 = productRepository.findById(savedEntity.getId()).block();
        ProductEntity entity2 = productRepository.findById(savedEntity.getId()).block();

        //Update the entity using the first entity object
        entity1.setName("n1");
        productRepository.save(entity1).block();

        //Update the entity using the second entity object
        StepVerifier.create(productRepository.save(entity2))
                .expectError(OptimisticLockingFailureException.class).verify();

        //Get the updated entity from the database and verify its new state.

        StepVerifier.create(productRepository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1
                && foundEntity.getName().equals("n1"))
                .verifyComplete();
    }
}
