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

import java.util.List;

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
    public void createCompositeProduct(ProductAggregate body) {
        try{
            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(),null);
            integration.createProduct(product);

            if(body.getRecommendations()!= null){
                body.getRecommendations().forEach(recommendationSummary -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), recommendationSummary.getRecommendationId(),
                            recommendationSummary.getAuthor(), recommendationSummary.getRate(), recommendationSummary.getContent(), null);
                    integration.createRecommendation(recommendation);
                });
            }
            if(body.getReviews()!=null){
                body.getReviews().forEach(reviewSummary -> {
                    Review review = new Review(body.getProductId(), reviewSummary.getReviewId(), reviewSummary.getAuthor(), reviewSummary.getSubject(), reviewSummary.getContent(), null);
                });
            }
        }catch (RuntimeException re){
            LOG.warn("Create Composite Product Failed", re);
            throw re;
        }

    }

    @Override
    public ProductAggregate getProduct(int productId) {
       Product product = integration.getProduct(productId);
       if(product ==null) throw new NotFoundException("No product found for productId: {}" + productId);

        List<Recommendation> recommendations = integration.getRecommendations(productId);

        List<Review> reviews = integration.getReviews(productId);

        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public void deleteCompositeProduct(int productId) {
        integration.deleteProduct(productId);
        integration.deleteRecommendations(productId);
        integration.deleteReviews(productId);
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
