package com.example.OrderService.service;

import com.example.OrderService.entity.Order;
import com.example.OrderService.exception.CustomException;
import com.example.OrderService.external.client.PaymentService;
import com.example.OrderService.external.client.ProductService;
import com.example.OrderService.external.request.PaymentRequest;
import com.example.OrderService.external.response.PaymentResponse;
import com.example.OrderService.external.response.ProductResponse;
import com.example.OrderService.model.OrderRequest;
import com.example.OrderService.model.OrderResponse;
import com.example.OrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Placing order Request: {}", orderRequest);
        productService.reduceQuantity(orderRequest.getProductId(),orderRequest.getQuantity());

        log.info("Creating Order with Status CREATED");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();

        order = orderRepository.save(order);
        log.info("Calling payment service to complete the payment");
        PaymentRequest paymentRequest =
                PaymentRequest.builder()
                        .orderId(order.getId())
                        .paymentMode(orderRequest.getPaymentMode())
                        .amount(orderRequest.getTotalAmount())
                        .build();
        String orderStatus = null;
        try{
            paymentService.doPayment(paymentRequest);
            log.info("Payment done successfully. Changing the order status to PLACED");
            orderStatus="PLACED";
        }catch(Exception e){
            log.error("Error occured in the payment. Changing the order status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        log.info("Order placed successfully with order id: {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(Long orderId) {

        log.info("Get order details for OrderId:{}",orderId);
        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(()->
                                new CustomException
                                        ("Order not found for the orderId:"+orderId,"NOT_FOUND",404)
                        );
        log.info("Invoking Product service to fetch the product for id:{}",order.getProductId());
        
        ProductResponse productResponse = restTemplate.getForObject(
        		"http://PRODUCT-SERVICE/product/"+ order.getProductId(),
        		ProductResponse.class);
        
        log.info("Getting payment information from the payment service");
        
        PaymentResponse paymentResponse = 
        		restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"+order.getId(), PaymentResponse.class);
        
        OrderResponse.ProductDetails productDetails = 
        		OrderResponse.ProductDetails.builder()
        		.productName(productResponse.getProductName())
        		.productId(productResponse.getProductId())
        		.price(productResponse.getPrice())
        		.quantity(productResponse.getQuantity())
        		.build();
        
        OrderResponse.PaymentDetails paymentDetails = 
        		OrderResponse.PaymentDetails.builder()
        		.paymentId(paymentResponse.getPaymentId())
        		.paymentStatus(paymentResponse.getStatus())
        		.paymentDate(paymentResponse.getPaymentDate())
        		.paymentMode(paymentResponse.getPaymentMode())
        		.build();

        OrderResponse orderResponse =
                OrderResponse.builder()
                        .orderId(order.getId())
                        .orderStatus(order.getOrderStatus())
                        .amount(order.getAmount())
                        .orderDate(order.getOrderDate())
                        .productDetails(productDetails)
                        .paymentDetails(paymentDetails)      
                        .build();
        return orderResponse;




    }
}
