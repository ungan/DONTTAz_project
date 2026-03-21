package com.donttaz.donttazapi.payment.controller.docs;

import com.donttaz.donttazapi.payment.dto.request.CreateMerchantRequest;
import com.donttaz.donttazapi.payment.dto.response.MerchantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Merchant", description = "Merchant management API")
public interface MerchantSwagger {

    @Operation(
            operationId = "createMerchant",
            summary = "Create merchant",
            description = "Creates a merchant for virtual payment testing.",
            security = {},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateMerchantRequest.class),
                            examples = @ExampleObject(
                                    name = "Create merchant request",
                                    value = """
                                            {
                                              "name": "이마트",
                                              "settlementAccountNo": "0204667768182760",
                                              "category": "MART"
                                            }
                                            """))))
    @ApiResponses({
             @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Merchant created"),
             @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Settlement account already exists")
    })
    ResponseEntity<MerchantResponse> createMerchant(@RequestBody CreateMerchantRequest request);

    @Operation(operationId = "getMerchants", summary = "Get merchants", description = "Returns all active merchants.", security = {})
    @ApiResponses({
             @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Merchant list returned")
    })
    ResponseEntity<List<MerchantResponse>> getMerchants();
}
