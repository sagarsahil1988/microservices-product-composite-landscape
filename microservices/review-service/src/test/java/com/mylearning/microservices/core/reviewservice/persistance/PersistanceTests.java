package com.mylearning.microservices.core.reviewservice.persistance;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PersistanceTests {

    @Autowired
    private ReviewRepository reviewRepository;

    private ReviewEntity savedEntity;

    @BeforeEach
    public void setUpDatabase(){
        reviewRepository.deleteAll();
        ReviewEntity entity = new ReviewEntity(1,2, "test_author", "test_subject", "test_content");
        savedEntity = reviewRepository.save(entity);

        assertEqualsReview(entity, savedEntity);
    }

    private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
        Assertions.assertEquals(expectedEntity.getId(), actualEntity.getId());
        Assertions.assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        Assertions.assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        Assertions.assertEquals(expectedEntity.getReviewId(), actualEntity.getReviewId());
        Assertions.assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        Assertions.assertEquals(expectedEntity.getContent(), actualEntity.getContent());
        Assertions.assertEquals(expectedEntity.getSubject(), actualEntity.getSubject());
    }

    @Test
    public void create(){
        ReviewEntity newEntity = new ReviewEntity(2, 3, "new_test_author", "new_test_subject", "new_test_content");
        reviewRepository.save(newEntity);

        ReviewEntity foundEntity = reviewRepository.findById(newEntity.getId()).get();
        assertEqualsReview(newEntity, foundEntity);
        Assertions.assertEquals(2, reviewRepository.count());
    }

    @Test
    public void update(){
        savedEntity.setAuthor("udpated_author");
        reviewRepository.save(savedEntity);
        ReviewEntity foundEntity = reviewRepository.findById(savedEntity.getId()).get();

        Assertions.assertEquals(1,foundEntity.getVersion());
        Assertions.assertEquals("udpated_author", foundEntity.getAuthor());
    }

    @Test
    public void delete(){
        reviewRepository.delete(savedEntity);
        Assertions.assertFalse(reviewRepository.existsById(savedEntity.getId()));
    }

    @Test
    public void getByProductId(){
        List<ReviewEntity> entityList = reviewRepository.findByProductId(savedEntity.getProductId());
        Assertions.assertFalse(entityList.isEmpty());
        assertEqualsReview(entityList.get(0), savedEntity);
    }

    @Test
    public void duplicateError(){
        ReviewEntity newEntity = new ReviewEntity(1,2, "new_test_author", "new_test_subject","new_test_content");
        Assertions.assertThrows(DataIntegrityViolationException.class, ()-> reviewRepository.save(newEntity));

    }

    @Test
    public void optimisticLockError(){
        //Store the entity into two separate entity objects
        ReviewEntity entity1 = reviewRepository.findById(savedEntity.getId()).get();
        ReviewEntity entity2 = reviewRepository.findById(savedEntity.getId()).get();

        //Update the entity using the first entity object
        entity1.setAuthor("a1");
        reviewRepository.save(entity1);
        //Update the entity using the second entity object
        //This should fail since the second entity now holds the old version number.
        try{
            entity2.setAuthor("a2");
            reviewRepository.save(entity2);
        }catch (OptimisticLockingFailureException ex)
        {
            ReviewEntity updatedEntity = reviewRepository.findById(savedEntity.getId()).get();
            Assertions.assertEquals(1,updatedEntity.getVersion());
            Assertions.assertEquals("a1", updatedEntity.getAuthor());
        }
    }
}
