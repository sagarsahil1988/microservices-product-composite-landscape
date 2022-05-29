package com.mylearning.microservices.core.productservice.services;

import com.mylearning.microservices.api_definition.core.product.Product;
import com.mylearning.microservices.core.productservice.persistance.ProductEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ProductMapper {

    @Mappings(
            @Mapping(target = "serviceAddress", ignore = true)
    )
    Product entityToApi(ProductEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    ProductEntity apiToEntity(Product api);
}

