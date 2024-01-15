package com.example.ProductService.exception;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class ProductServiceCustomException extends RuntimeException{

    @Autowired
    private String errorCode;

    public ProductServiceCustomException(String message, String errorCode){
        super (message);
        this.errorCode = errorCode;
    }
}
