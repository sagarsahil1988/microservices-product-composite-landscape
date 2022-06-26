package com.mylearning.microservices.core.productcompositeservice.services;

import com.mylearning.microservices.api_definition.composite.product.*;
import com.mylearning.microservices.api_definition.core.product.Product;
import com.mylearning.microservices.api_definition.core.recommendation.Recommendation;
import com.mylearning.microservices.api_definition.core.review.Review;
import com.mylearning.microservices.core.utility.exceptions.NotFoundException;
import com.mylearning.microservices.core.utility.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

    private ServiceUtil serviceUtil;
    private ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeServiceImpl(ProductCompositeIntegration integration, ServiceUtil serviceUtil){
        this.integration = integration;
        this.serviceUtil = serviceUtil;
    }


    @Override
    public Mono<Void> createCompositeProduct(ProductAggregate body) {

        try{
            List<Mono> monoList = new ArrayList<>();
            LOG.debug("Create Composite Product: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            monoList.add(integration.createProduct(product));
            if(body.getRecommendations()!=null){
                body.getRecommendations().forEach(r ->{
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(),r.getRate(),
                    r.getContent(),null);
                    monoList.add(integration.createRecommendation(recommendation));
                });
            }
            if(body.getReviews()!=null){
                body.getReviews().forEach(re ->{
                    Review review = new Review(body.getProductId(), re.getReviewId(),re.getAuthor(), re.getSubject(),re.getContent(),null);
                    monoList.add(integration.createReview(review));
                });
            }

            LOG.debug("Create Composite Product: composite entities created for productId: {}", body.getProductId());

            return Mono.zip(r ->"",monoList.toArray(new Mono[0]))
                    .doOnError(ex -> LOG.warn("Create Composite Product Failed: {} ", ex.toString()))
                    .then();
        }catch (RuntimeException runtimeException){
            LOG.warn("Create Composite Product Failed: {}", runtimeException.toString());
            throw runtimeException;
        }

    }

    @Override
    public Mono<Void> deleteCompositeProduct(int productId) {

        try {
            LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);
            return Mono.zip(
                    r -> "",
            integration.deleteProduct(productId),
            integration.deleteRecommendations(productId),
            integration.deleteReviews(productId))
                    .doOnError(ex -> LOG.warn("delete failed: {}", ex.toString()))
                    .log(LOG.getName(), Level.FINE).then();

        }catch (RuntimeException re) {
            LOG.warn("deleteCompositeProduct failed: {}", re.toString());
            throw re;
        }

    }

    @Override
    public Mono<ProductAggregate> getProduct(int productId) {

        LOG.info("Will get composite product info for productId: {}", productId);

        return Mono.zip(
                values -> createProductAggregate((Product) values[0], (List<Recommendation>) values[1],
                        (List<Review>) values[2], serviceUtil.getServiceAddress()),
                integration.getProduct(productId),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList())
                .doOnError(ex -> LOG.warn("get Composite product failed: {} ", ex.toString()))
                .log(LOG.getName(), Level.FINE);
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {
        //1. Set up product information
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        //2. Copy summary recommendation information if available
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                recommendations.stream().map(r -> new RecommendationSummary(r.getRecommendationId(),r.getAuthor(),r.getRate(),r.getContent())).collect(Collectors.toList());

        //3. Copy summary review information if available
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
                reviews.stream().map(review -> new ReviewSummary(review.getReviewId(), review.getAuthor(), review.getSubject(),review.getContent())).collect(Collectors.toList());

        //4. Create info regarding the involved micro-service address
        String productServiceAddress = product.getServiceAddress();
        String recommendationServiceAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        String reviewServiceAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";

        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productServiceAddress, reviewServiceAddress, recommendationServiceAddress);

        return new ProductAggregate(productId, name, weight, reviewSummaries, recommendationSummaries, serviceAddresses);

    }
}
