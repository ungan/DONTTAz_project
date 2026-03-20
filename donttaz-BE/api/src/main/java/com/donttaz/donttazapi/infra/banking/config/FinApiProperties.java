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

    private String apiKey;

    @NotBlank
    @Size(max = 20)
    private String oneWonAuthText;

    @Positive
    private int oneWonExpirationSeconds;

    @Positive
    private int connectTimeoutMillis = 3000;

    @Positive
    private int readTimeoutMillis = 10000;
}