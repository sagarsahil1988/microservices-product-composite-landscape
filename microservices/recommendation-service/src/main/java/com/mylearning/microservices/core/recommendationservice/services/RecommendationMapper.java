package com.mylearning.microservices.core.recommendationservice.services;

import com.mylearning.microservices.api_definition.core.recommendation.Recommendation;
import com.mylearning.microservices.core.recommendationservice.persistance.RecommendationEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface RecommendationMapper {

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)

    })
    Recommendation entityToApi(RecommendationEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    RecommendationEntity apiToEntity(Recommendation api);

    List<Recommendation> entityListToApiList(List<RecommendationEntity> entityList);
    List<RecommendationEntity> apiListToEntityList(List<Recommendation> apiList);
}
