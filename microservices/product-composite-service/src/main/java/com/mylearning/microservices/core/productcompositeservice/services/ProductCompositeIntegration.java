package com.mylearning.microservices.core.productcompositeservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.microservices.api_definition.core.product.Product;
import com.mylearning.microservices.api_definition.core.product.ProductService;
import com.mylearning.microservices.api_definition.core.recommendation.Recommendation;
import com.mylearning.microservices.api_definition.core.recommendation.RecommendationService;
import com.mylearning.microservices.api_definition.core.review.Review;
import com.mylearning.microservices.api_definition.core.review.ReviewService;
import com.mylearning.microservices.core.utility.exceptions.InvalidInputEception;
import com.mylearning.microservices.core.utility.exceptions.NotFoundException;
import com.mylearning.microservices.core.utility.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;


    @Autowired
    public ProductCompositeIntegration(
            RestTemplate restTemplate, ObjectMapper objectMapper,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") String productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") String recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") String reviewServicePort
    ){
        LOG.info("Initializing ProductComposite Constructor!!!");
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        productServiceUrl = "http://" + productServiceHost+ ":" + productServicePort + "/product/";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";

    }

    @Override
    public Product createProduct(Product body) {
        try {
            return restTemplate.postForObject(productServiceUrl, body, Product.class);
        }catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
    }



    @Override
    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl + productId;
            LOG.debug("Will call getProductAPI on url : {}", url);

            Product product = restTemplate.getForObject(url, Product.class);
            LOG.debug("Found a product with id: {}", product.getProductId());

            return product;
        }catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try{
            restTemplate.delete(productServiceUrl + "/" + productId);
        }catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
    }


    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try {
            LOG.debug("Will post a new recommendation to URL : {}", recommendationServiceUrl);
            Recommendation recommendation = restTemplate.postForObject(recommendationServiceUrl, body, Recommendation.class);
            LOG.debug("Created a recommendation with id: {}", recommendation.getProductId());

            return recommendation;
        }catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try{
            String url = recommendationServiceUrl + productId;
            LOG.debug("Will call get Recommendation API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate.exchange(url, HttpMethod.GET,
                    null, new ParameterizedTypeReference<List<Recommendation>>() {}).getBody();
            LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
            return recommendations;
        }catch (Exception ex){
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + "?productId=" + productId;
            LOG.debug("Will call the deleteRecommendation API on url : {}", url);
            restTemplate.delete(url);
        }catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Review createReview(Review body) {
        try {
            LOG.debug("Will post a new review to url: {}", reviewServiceUrl);
            Review review = restTemplate.postForObject(reviewServiceUrl, body, Review.class);
            LOG.debug("Created a review with id: {}", review.getProductId());
            return review;
        }catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try{
            String url = reviewServiceUrl + productId;
            LOG.debug("Will call get Review Api in url: {}", url);
            List<Review> reviews = restTemplate.exchange(url, HttpMethod.GET,
                    null, new ParameterizedTypeReference<List<Review>>() {}).getBody();
            LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            return reviews;
        }catch (Exception ex){
            LOG.warn("Got an exception while requesting reviews, retrun zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteReviews(int productId) {
        try{
            String url = reviewServiceUrl + "?productId=" + productId;
            LOG.debug("Will call the deleteReview API on url: {}", url);
            restTemplate.delete(url);
        }catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
    }


    private String getErrorMessage(HttpClientErrorException ex) {
        try{
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }catch (IOException ioex){
            return   ioex.getMessage();
        }
    }
    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (ex.getStatusCode()){
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(ex));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputEception(getErrorMessage(ex));

            default:
                LOG.warn("Got a unexpected Http error: {}, will throw it", ex.getStatusCode());
                LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                return ex;
        }
    }

}



