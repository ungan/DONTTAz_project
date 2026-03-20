package com.donttaz.donttazapi.payment.controller;

import com.donttaz.donttazapi.payment.controller.docs.PaymentSwagger;
import com.donttaz.donttazapi.payment.dto.request.CreatePaymentRequest;
import com.donttaz.donttazapi.payment.dto.response.PaymentResponse;
import com.donttaz.donttazapi.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentSwagger {

    private final PaymentService paymentService;

    @PostMapping
    @Override
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(request));
    }
}