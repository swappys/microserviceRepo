package com.example.ProductService.service;

import com.example.ProductService.entity.Product;
import com.example.ProductService.exception.ProductServiceCustomException;
import com.example.ProductService.model.ProductRequest;
import com.example.ProductService.model.ProductResponse;
import com.example.ProductService.repository.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.websocket.server.ServerEndpoint;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("Adding Product..");
        Product product = Product.builder()
                .productName(productRequest.getName())
                .quantity(productRequest.getQuantity())
                .price(productRequest.getPrice())
                .build();

        productRepository.save(product);
        log.info("Product Created");
        return product.getProductId();
    }

    @Override
    public ProductResponse getProductById(long productId) {
        log.info("Get the product for product id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ProductServiceCustomException("Product with the given id not found","PRODUCT_NOT_FOUND"));

        ProductResponse productResponse
                = new ProductResponse();
        BeanUtils.copyProperties(product,productResponse);

        return productResponse;
    }

    @Override
    public void reduceQuantity(long productId, long quantity) {
        log.info("Reduce quantity {} for id {}", quantity,productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomException("Product qith the given id not found","PRODUCT NOT FOUND"));

        if(product.getQuantity()<quantity){
            throw new ProductServiceCustomException(
                    "Product does not have sufficient quantity",
                    "INSUFFICIENT QUANTITY"
            );
        }
        product.setQuantity(product.getQuantity()-quantity);
        productRepository.save(product);
        log.info("Product Quantity updated successfully");
    }
}
