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

import java.util.ArrayList;
import java.util.List;

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
    public Recommendation createRecommendation(Recommendation body) {
        try{
            RecommendationEntity entity = mapper.apiToEntity(body);
            RecommendationEntity newEntity = recommendationRepository.save(entity);
            LOG.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
            return mapper.entityToApi(newEntity);
        }catch (DuplicateKeyException dke){
            throw new InvalidInputEception("Duplicate Key, Product Id: " + body.getProductId() + ", Recommendation Id: " + body.getRecommendationId());
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if(productId < 1) throw new InvalidInputEception("Invalid productId: " + productId);
        LOG.debug("Starting to look for recommendations for productId: {}", productId);
        List<RecommendationEntity> entityList = recommendationRepository.findByProductId(productId);
        List<Recommendation> list = mapper.entityListToApiList(entityList);
        list.forEach(e-> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getRecommendations: response size: {}", list.size());
        return list;
    }

    @Override
    public void deleteRecommendations(int productId) {
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with product Id: {}", productId);
        recommendationRepository.deleteAll(recommendationRepository.findByProductId(productId));
    }
}
