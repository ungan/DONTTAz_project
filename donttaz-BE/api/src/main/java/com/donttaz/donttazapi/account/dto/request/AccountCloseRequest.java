package com.donttaz.donttazapi.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Account close request")
public record AccountCloseRequest(
        @Schema(description = "Refund bank code", example = "032")
        @NotBlank(message = "Refund bank code is required.")
        @Pattern(regexp = "\\d{3}", message = "Refund bank code must be a 3-digit number.")
        String refundBankCode,

        @Schema(description = "Refund account number", example = "0324003842129948")
        @NotBlank(message = "Refund account number is required.")
        @Pattern(regexp = "^[0-9]+$", message = "Refund account number must contain digits only.")
        String refundAccountNo
) {
}
