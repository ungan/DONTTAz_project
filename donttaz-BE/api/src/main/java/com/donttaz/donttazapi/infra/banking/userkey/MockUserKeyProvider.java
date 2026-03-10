package com.donttaz.donttazapi.infra.banking.userkey;

import com.donttaz.donttazapi.infra.banking.config.FinApiProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class MockUserKeyProvider implements UserKeyProvider {

    private final FinApiProperties finApiProperties;

    public MockUserKeyProvider(FinApiProperties finApiProperties) {
        this.finApiProperties = finApiProperties;
    }

    @Override
    public String getUserKey(Long userId) {
        return finApiProperties.getMockUserKey();
    }
}
