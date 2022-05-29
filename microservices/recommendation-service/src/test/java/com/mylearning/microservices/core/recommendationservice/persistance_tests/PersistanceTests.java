package com.mylearning.microservices.core.recommendationservice.persistance_tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mylearning.microservices.core.recommendationservice.persistance.RecommendationEntity;
import com.mylearning.microservices.core.recommendationservice.persistance.RecommendationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PersistanceTests {
/*
    @Autowired
    private RecommendationRepository recommendationRepository;

    private RecommendationEntity savedEntity;

    @BeforeEach
    public void setUpDatabase(){
        recommendationRepository.deleteAll();
        RecommendationEntity entity = new RecommendationEntity(1,2,"test_author", 3, "test content");
        savedEntity = recommendationRepository.save(entity);
        assertEqualsRecommendation(entity, savedEntity);
    }

    private void assertEqualsRecommendation(RecommendationEntity entity, RecommendationEntity savedEntity) {
        Assertions.assertEquals(entity.getId(), savedEntity.getId());
        Assertions.assertEquals(entity.getProductId(), savedEntity.getProductId());
        Assertions.assertEquals(entity.getAuthor(), savedEntity.getAuthor());
        Assertions.assertEquals(entity.getContent(),savedEntity.getContent());
        Assertions.assertEquals(entity.getRate(), savedEntity.getRate());
    }

    @Test
    public void create(){
        RecommendationEntity newEntity = new RecommendationEntity(1,3,"new test autor", 4, "new test content");
        recommendationRepository.save(newEntity);

        RecommendationEntity foundEntity = recommendationRepository.findById(newEntity.getId()).get();
        assertEqualsRecommendation(newEntity,foundEntity);

        Assertions.assertEquals(2, recommendationRepository.count());
    }

    @Test
    public void update(){
        savedEntity.setAuthor("updated author");
        recommendationRepository.save(savedEntity);

        RecommendationEntity foundEntity = recommendationRepository.findById(savedEntity.getId()).get();
        Assertions.assertEquals("updated author", foundEntity.getAuthor());
        Assertions.assertEquals(1,foundEntity.getVersion());
    }
    @Test
    public void delete(){
        recommendationRepository.delete(savedEntity);
        Assertions.assertFalse(recommendationRepository.existsById(savedEntity.getId()));
    }

    @Test
    public void getByProductId(){
        List<RecommendationEntity> entityList = recommendationRepository.findByProductId(savedEntity.getProductId());
        Assertions.assertFalse(entityList.isEmpty());
        assertEqualsRecommendation(entityList.get(0), savedEntity);
    }

    @Test
    public void duplicateError(){
        RecommendationEntity entity = new RecommendationEntity(savedEntity.getProductId(), savedEntity. getRecommendationId(),"test author", 10, "test content");
        Assertions.assertThrows(DuplicateKeyException.class, ()-> recommendationRepository.save(entity));
    }

    @Test
    public void optimisticLockError() throws JsonProcessingException {
        //Store the entity in two separate entity objects

        RecommendationEntity entity1 = recommendationRepository.findById(savedEntity.getId()).get();
        RecommendationEntity entity2 = recommendationRepository.findById(savedEntity.getId()).get();

        //Update the entity using the first entity object.
        entity1.setAuthor("a1");
        recommendationRepository.save(entity1);
        //Update the entity using the second entity object
        //This should fail since the second entity hold the old version number.
        try{
            entity2.setAuthor("a2");
            recommendationRepository.save(entity2);
        } catch (OptimisticLockingFailureException ex){
            //Get the updated entity from database and verify its new state
            RecommendationEntity updatedEntity =
                    recommendationRepository.findById(savedEntity.getId()).get();
            Assertions.assertEquals(1,(int)updatedEntity.getVersion());
            Assertions.assertEquals("a1", updatedEntity.getAuthor());
        }
    }*/
}
