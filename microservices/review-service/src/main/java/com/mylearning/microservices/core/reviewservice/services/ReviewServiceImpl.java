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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final ReviewMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl (ReviewRepository reviewRepository, ReviewMapper mapper, ServiceUtil serviceUtil){
        this.reviewRepository = reviewRepository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }


    @Override
    public Review createReview(Review body) {
        try{
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = reviewRepository.save(entity);
            LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(),body.getReviewId());
            return mapper.entityToApi(newEntity);
        }catch (DataIntegrityViolationException dive){
            throw new InvalidInputEception(
                    "Duplicate key, Product Id: " + body.getProductId() + ", Review Id: "+ body.getReviewId());
        }

    }

    @Override
    public List<Review> getReviews(int productId) {
        if (productId < 1) throw new InvalidInputEception("Invalid productId: "+ productId);

        List<ReviewEntity> entityList = reviewRepository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e->e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getReviews: response size: {}", list.size());
        return list;
    }

    @Override
    public void deleteReviews(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for the product with product Id: {}", productId);
        reviewRepository.deleteAll(reviewRepository.findByProductId(productId));

    }
}
