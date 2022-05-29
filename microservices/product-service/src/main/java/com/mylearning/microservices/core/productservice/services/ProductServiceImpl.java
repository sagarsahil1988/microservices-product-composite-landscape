package com.mylearning.microservices.core.productservice.services;


import com.mylearning.microservices.api_definition.core.product.Product;
import com.mylearning.microservices.api_definition.core.product.ProductService;
import com.mylearning.microservices.core.productservice.persistance.ProductEntity;
import com.mylearning.microservices.core.productservice.persistance.ProductRepository;
import com.mylearning.microservices.core.utility.exceptions.InvalidInputEception;
import com.mylearning.microservices.core.utility.exceptions.NotFoundException;
import com.mylearning.microservices.core.utility.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductMapper mapper;
    private final ServiceUtil serviceUtil;


    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductMapper mapper, ServiceUtil serviceUtil){
        this.productRepository = productRepository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;

    }

    @Override
    public Product createProduct(Product body) {
        try{
            ProductEntity entity = mapper.apiToEntity(body);
            ProductEntity newEntity = productRepository.save(entity);
            return mapper.entityToApi(newEntity);
        }catch (DuplicateKeyException dke){
            throw new InvalidInputEception("Duplicate Key , Product Id : " + body.getProductId());
        }
    }

    @Override
    public Product getProduct(int productId) {

        LOG.debug("/product return the found product for productId ={}", productId);

        if (productId < 1 ) throw new InvalidInputEception("Invalid ProductId: " + productId);

        ProductEntity entity = productRepository.findByProductId(productId).orElseThrow(()-> new NotFoundException("No Product found for productId: " + productId));
        Product response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());
        return response;
    }

    @Override
    public void deleteProduct(int productId) {
        productRepository.findByProductId(productId).ifPresent(p-> productRepository.delete(p));

    }
}
