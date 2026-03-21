package com.donttaz.donttazapi.payment.controller.docs;

import com.donttaz.donttazapi.payment.dto.request.CreatePaymentRequest;
import com.donttaz.donttazapi.payment.dto.response.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Payment", description = "Virtual payment API")
public interface PaymentSwagger {

    @Operation(
            operationId = "createPayment",
            summary = "Create payment",
            description = "Transfers funds to the merchant settlement account and stores the payment mapping.",
            security = {},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreatePaymentRequest.class),
                            examples = @ExampleObject(
                                    name = "Create payment request",
                                    value = """
                                            {
                                              "withdrawalAccountNo": "0016174648358792",
                                              "merchantId": 1,
                                              "amount": "10000"
                                            }
                                            """))))
    @ApiResponses({
             @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Payment created"),
             @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Merchant or account not found")
    })
    ResponseEntity<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request);
}
