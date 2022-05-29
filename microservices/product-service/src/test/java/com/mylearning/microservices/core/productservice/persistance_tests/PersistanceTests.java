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
        productRepository.deleteAll();
        ProductEntity productEntity = new ProductEntity(1,"testProductName1",1);
        savedEntity = productRepository.save(productEntity);
        assertEqualsProduct(productEntity,savedEntity);
    }

    @Test
    public void create(){
        ProductEntity newEntity = new ProductEntity(2,"testProductName2",2);
        productRepository.save(newEntity);

        ProductEntity foundEntity = productRepository.findById(newEntity.getId()).get();
        assertEqualsProduct(newEntity,foundEntity);
        Assertions.assertEquals(2,productRepository.count());

    }

    @Test
    public void update(){
        savedEntity.setName("updated_name_for_saved_entity");
        productRepository.save(savedEntity);

        ProductEntity foundEntity = productRepository.findById(savedEntity.getId()).get();
        Assertions.assertEquals("updated_name_for_saved_entity",foundEntity.getName());
        Assertions.assertEquals(1,foundEntity.getVersion());

    }

    @Test
    public void delete(){
        productRepository.delete(savedEntity);
        Assertions.assertFalse(productRepository.existsById(savedEntity.getId()));
    }

    @Test
    public void getByProductId(){
        Optional<ProductEntity> entity = productRepository.findByProductId(savedEntity.getProductId());
        Assertions.assertTrue(entity.isPresent());
        assertEqualsProduct(savedEntity,entity.get());
    }

    @Test
    public void duplicateError(){
            ProductEntity entity = new ProductEntity(savedEntity.getProductId(),"name", 10);
            Assertions.assertThrows(DuplicateKeyException.class,()-> productRepository.save(entity));
    }

    @Test
    public void optimisticLockError(){
        //Store the Entity into two seperate Entity Object
        ProductEntity entity1 = productRepository.findById(savedEntity.getId()).get();
        ProductEntity entity2 = productRepository.findById(savedEntity.getId()).get();

        //Update the entity using the first entity object
        entity1.setName("n1");
        productRepository.save(entity1);

        //Update the entity using the second entity object
        try {
            entity2.setName("n2");
            productRepository.save(entity2);

        }catch (OptimisticLockingFailureException ex){}
        //Get the updated entity from the database and verify its new state.

        ProductEntity updatedEntity = productRepository.findById(savedEntity.getId()).get();
        Assertions.assertEquals(1, updatedEntity.getVersion());
        Assertions.assertEquals("n1",updatedEntity.getName());
    }

    @Test
    public void paging(){
        productRepository.deleteAll();
        List<ProductEntity> newPorducts = rangeClosed(1001,1010)
                .mapToObj(i-> new ProductEntity(i, "name"+i, i)).collect(Collectors.toList());
        productRepository.saveAll(newPorducts);
        Pageable nextPage = PageRequest.of(0,4, Sort.Direction.ASC,"productId");
        nextPage = testNextPage(nextPage,"[1001, 1002, 1003, 1004]",true);
        

    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productPage = productRepository.findAll(nextPage);
        Assertions.assertEquals(expectedProductIds,
                productPage.getContent().stream().map(p->p.getProductId()).collect(Collectors.toList()).toString());
        Assertions.assertEquals(expectsNextPage, productPage.hasNext());
        return productPage.nextPageable();
    }

    private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
        Assertions.assertEquals(expectedEntity.getId(),actualEntity.getId());
        Assertions.assertEquals(expectedEntity.getVersion(),actualEntity.getVersion());
        Assertions.assertEquals(expectedEntity.getProductId(),actualEntity.getProductId());
        Assertions.assertEquals(expectedEntity.getName(),actualEntity.getName());
        Assertions.assertEquals(expectedEntity.getWeight(),actualEntity.getWeight());
    }
}
