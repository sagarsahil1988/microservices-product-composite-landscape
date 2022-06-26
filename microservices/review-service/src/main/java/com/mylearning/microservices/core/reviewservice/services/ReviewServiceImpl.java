package com.mylearning.microservices.core.reviewservice.services;

import com.mylearning.microservices.api_definition.core.review.Review;
import com.mylearning.microservices.api_definition.core.review.ReviewService;
import com.mylearning.microservices.core.reviewservice.persistance.ReviewEntity;
import com.mylearning.microservices.core.reviewservice.persistance.ReviewRepository;
import com.mylearning.microservices.core.utility.exceptions.InvalidInputEception;
import com.mylearning.microservices.core.utility.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.swing.text.LabelView;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final ReviewMapper mapper;
    private final ServiceUtil serviceUtil;

    private final Scheduler jdbcScheduler;

    @Autowired
    public ReviewServiceImpl (@Qualifier("jdbcScheduler") Scheduler jdbcScheduler, ReviewRepository reviewRepository,
                              ReviewMapper mapper, ServiceUtil serviceUtil){
        this.jdbcScheduler = jdbcScheduler ;
        this.reviewRepository = reviewRepository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }


    @Override
    public Mono<Review> createReview(Review body) {
     if(body.getProductId() < 1){
         throw  new InvalidInputEception("Invalid productId: " + body.getProductId());
     }
     return Mono.fromCallable(()-> internalCreateReview(body))
             .subscribeOn(jdbcScheduler);
    }

    private Review internalCreateReview(Review body) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntiry = reviewRepository.save(entity);
            LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return mapper.entityToApi(newEntiry);
        }catch (DataIntegrityViolationException dive) {
            throw new InvalidInputEception("Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" + body.getReviewId());
        }
    }


    @Override
    public Flux<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputEception("Invalid productId: "+ productId);
        }
        LOG.info("Will get reviews for product with id = {}",productId);

        return Mono.fromCallable(()-> internalGetReviews(productId))
                .flatMapMany(Flux::fromIterable)
                .log(LOG.getName(), Level.FINE)
                .subscribeOn(jdbcScheduler);
    }

    private List<Review> internalGetReviews(int productId) {
        List<ReviewEntity> reviewEntityList = reviewRepository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(reviewEntityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("Response Size : {}", list.size());
        return list;
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        if(productId < 1){
            throw new InvalidInputEception("Invalid productId: " + productId);
        }

        return Mono.fromRunnable(()-> internalDeleteReviews(productId))
                .subscribeOn(jdbcScheduler).then();

    }

    private void internalDeleteReviews(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for the product with product Id: {}", productId);
        reviewRepository.deleteAll(reviewRepository.findByProductId(productId));
    }
}
