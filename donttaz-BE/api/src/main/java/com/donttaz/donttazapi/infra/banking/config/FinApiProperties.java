package com.donttaz.donttazapi.infra.banking.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "fin-api")
public class FinApiProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String institutionCode;

    @NotBlank
    private String fintechAppNo;

    /**
     * App-wide finAPI key. This is expected to be injected from environment or secret storage.
     */
    private String apiKey;

    @NotBlank
    @Size(max = 20)
    private String oneWonAuthText;

    @Positive
    private int oneWonExpirationSeconds;
}
