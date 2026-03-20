package com.donttaz.donttazapi.payment.controller.docs;

import com.donttaz.donttazapi.payment.dto.request.CreateMerchantRequest;
import com.donttaz.donttazapi.payment.dto.response.MerchantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Merchant", description = "Merchant management API")
public interface MerchantSwagger {

    @Operation(summary = "Create merchant", description = "Creates a merchant for virtual payment testing.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Merchant created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Settlement account already exists")
    })
    ResponseEntity<MerchantResponse> createMerchant(@RequestBody CreateMerchantRequest request);

    @Operation(summary = "Get merchants", description = "Returns all active merchants.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Merchant list returned")
    })
    ResponseEntity<List<MerchantResponse>> getMerchants();
}