package com.donttaz.donttazapi.account.controller;

import com.donttaz.donttazapi.account.dto.request.CreateDemandDepositProductRequest;
import com.donttaz.donttazapi.account.dto.response.DemandDepositProductListResponse;
import com.donttaz.donttazapi.account.dto.response.DemandDepositProductResponse;
import com.donttaz.donttazapi.account.service.DemandDepositService;
import com.donttaz.donttazapi.account.service.dto.DemandDepositProductResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts/demand-deposits")
@Tag(name = "수시입출금 상품", description = "수시입출금 상품 관리 API")
public class DemandDepositController {

        private final DemandDepositService demandDepositService;

        public DemandDepositController(DemandDepositService demandDepositService) {
                this.demandDepositService = demandDepositService;
        }

        @PostMapping("/products")
        @Operation(summary = "수시입출금 상품 등록", description = "finAPI에 수시입출금 상품을 등록합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateDemandDepositProductRequest.class), examples = @ExampleObject(name = "상품 등록 요청", value = """
                        {
                          "bankCode": "001",
                          "accountName": "한국은행 수시입출금 상품명",
                          "accountDescription": "한국은행 수시입출금 상품설명"
                        }
                        """))))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "상품 등록 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
        })
        public ResponseEntity<DemandDepositProductResponse> createProduct(
                        @Valid @RequestBody CreateDemandDepositProductRequest request) {
                DemandDepositProductResult result = demandDepositService.createProduct(
                                request.bankCode(),
                                request.accountName(),
                                request.accountDescription());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(toProductResponse(result));
        }

        @GetMapping("/products")
        @Operation(summary = "수시입출금 상품 조회", description = "finAPI에 등록된 수시입출금 상품 목록을 조회합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
        })
        public ResponseEntity<DemandDepositProductListResponse> getProducts() {
                List<DemandDepositProductResponse> products = demandDepositService.getProducts().stream()
                                .map(this::toProductResponse)
                                .toList();

                return ResponseEntity.ok(
                                DemandDepositProductListResponse.builder()
                                                .products(products)
                                                .build());
        }

        private DemandDepositProductResponse toProductResponse(DemandDepositProductResult result) {
                return DemandDepositProductResponse.builder()
                                .accountTypeUniqueNo(result.accountTypeUniqueNo())
                                .bankCode(result.bankCode())
                                .bankName(result.bankName())
                                .accountTypeCode(result.accountTypeCode())
                                .accountTypeName(result.accountTypeName())
                                .accountName(result.accountName())
                                .accountDescription(result.accountDescription())
                                .accountType(result.accountType())
                                .build();
        }
}
