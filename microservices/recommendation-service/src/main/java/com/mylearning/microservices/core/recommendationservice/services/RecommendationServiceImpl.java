package com.mylearning.microservices.core.recommendationservice.services;

import com.mylearning.microservices.api_definition.core.recommendation.Recommendation;
import com.mylearning.microservices.api_definition.core.recommendation.RecommendationService;
import com.mylearning.microservices.core.recommendationservice.persistance.RecommendationEntity;
import com.mylearning.microservices.core.recommendationservice.persistance.RecommendationRepository;
import com.mylearning.microservices.core.utility.exceptions.InvalidInputEception;
import com.mylearning.microservices.core.utility.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final RecommendationRepository recommendationRepository;
    private final RecommendationMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl (RecommendationRepository recommendationRepository, RecommendationMapper mapper, ServiceUtil serviceUtil){
        this.recommendationRepository = recommendationRepository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        if(body.getProductId() < 1){
            throw new InvalidInputEception("Invalid productId: " + body.getProductId());
        }
        RecommendationEntity entity = mapper.apiToEntity(body);
        Mono<Recommendation> newEntity =
                recommendationRepository.save(entity)
                        .log(LOG.getName(), Level.FINE)
                        .onErrorMap(
                                DuplicateKeyException.class,
                                ex -> new InvalidInputEception("Duplicate Key, Product Id: " + body.getProductId()
                                        + "Recommendation Id: " + body.getRecommendationId())
                        ).map(e -> mapper.entityToApi(e));
        return newEntity;
    }
    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        if(productId < 1) {
            throw new InvalidInputEception("Invalid productId: " + productId);
        }

        LOG.info("Starting to look for recommendations for productId: {}", productId);
        return recommendationRepository.findByProductId(productId)
                .log(LOG.getName(),Level.FINE)
                .map(e -> mapper.entityToApi(e))
                .map(e -> {
                    e.setServiceAddress(serviceUtil.getServiceAddress());
                    return e;
                });
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {

        if(productId < 1){
            throw new InvalidInputEception("Invalid productId :" + productId);
        }
        LOG.info("deleteRecommendations: tries to delete recommendations for the product with product Id: {}", productId);
         return recommendationRepository.deleteAll(recommendationRepository.findByProductId(productId));
    }
}
