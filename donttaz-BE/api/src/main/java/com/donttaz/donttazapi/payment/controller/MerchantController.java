package com.donttaz.donttazapi.payment.controller;

import com.donttaz.donttazapi.payment.controller.docs.MerchantSwagger;
import com.donttaz.donttazapi.payment.dto.request.CreateMerchantRequest;
import com.donttaz.donttazapi.payment.dto.response.MerchantResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import com.donttaz.donttazapi.payment.service.MerchantService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants")
@SecurityRequirements({})
@RequiredArgsConstructor
public class MerchantController implements MerchantSwagger {

    private final MerchantService merchantService;

    @PostMapping
    @Override
    public ResponseEntity<MerchantResponse> createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(merchantService.createMerchant(request));
    }

    @GetMapping
    @Override
    public ResponseEntity<List<MerchantResponse>> getMerchants() {
        return ResponseEntity.ok(merchantService.getMerchants());
    }
}
