package com.example.OrderService.model;

import lombok.*;

import java.time.Instant;

import com.example.OrderService.entity.Order.OrderBuilder;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private long orderId;
    private Instant orderDate;
    private String orderStatus;
    private long amount;
    private ProductDetails productDetails;
    private PaymentDetails paymentDetails;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentDetails {
    	
    	private long paymentId;
    	private PaymentMode paymentMode;
    	private String paymentStatus;
    	private Instant paymentDate;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDetails {

        private String productName;
        private long productId;
        private long quantity;
        private long price;
    }
    


}
