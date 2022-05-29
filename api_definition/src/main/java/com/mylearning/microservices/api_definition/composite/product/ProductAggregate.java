package com.mylearning.microservices.api_definition.composite.product;

import java.util.List;

public class ProductAggregate {
    private final int productId;
    private final String name;
    private final int weight;
    private final List<ReviewSummary> reviews;
    private final List<RecommendationSummary> recommendations;
    private final ServiceAddresses serviceAddess;

    public ProductAggregate(
            int productId,
            String name,
            int weight,
            List<ReviewSummary> reviews,
            List<RecommendationSummary> recommendations,
            ServiceAddresses serviceAddess) {
        this.productId = productId;
        this.name = name;
        this.weight = weight;
        this.reviews = reviews;
        this.recommendations = recommendations;
        this.serviceAddess = serviceAddess;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public List<ReviewSummary> getReviews() {
        return reviews;
    }

    public List<RecommendationSummary> getRecommendations() {
        return recommendations;
    }

    public ServiceAddresses getServiceAddess() {
        return serviceAddess;
    }
}
