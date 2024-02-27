package com.example.PaymentService.service;

import com.example.PaymentService.model.PaymentRequest;

public interface PaymentService {
    Long doPayment(PaymentRequest paymentRequest);
}
