package com.donttaz.donttazapi.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "돈타즈 API",
                version = "v1",
                description = "SSAFY finAPI와 연동된 수시입출금 계좌 API 문서입니다.",
                contact = @Contact(name = "Donttaz Backend")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "로컬 서버")
        }
)
public class OpenApiConfig {
}
