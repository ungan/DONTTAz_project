package com.donttaz.donttazapi.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortOneIdentityResponse {
    private String id;
    private String status; // "VERIFIED", "FAILED", "READY" 등
    private VerifiedCustomer verifiedCustomer;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifiedCustomer {
        private String name;
        private String phoneNumber;
        private String ci; // 주민번호 대체 키 (본인인증 고유값)
    }
}
