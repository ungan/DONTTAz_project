package com.donttaz.donttazapi.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
@OpenAPIDefinition(info = @Info(title = "돈타즈 API", version = "v1", description = "SSAFY finAPI와 연동된 수시입출금 계좌 API 문서입니다.", contact = @Contact(name = "Donttaz Backend")), servers = {
        @Server(url = "http://localhost:8080", description = "로컬 서버"),
        @Server(url = "/", description = "운영 서버 (자동 연동)")
}, security = {
        @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
})
public class SwaggerConfig {

    @Bean
    public OpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> openApi.getPaths().values()
                .forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                    ApiResponses apiResponses = operation.getResponses();

                    if (!apiResponses.containsKey("400")) {
                        apiResponses.addApiResponse("400", createApiResponse("Bad Request"));
                    }
                    if (!apiResponses.containsKey("401")) {
                        apiResponses.addApiResponse("401", createApiResponse("Unauthorized"));
                    }
                    if (!apiResponses.containsKey("403")) {
                        apiResponses.addApiResponse("403", createApiResponse("Forbidden"));
                    }
                    if (!apiResponses.containsKey("404")) {
                        apiResponses.addApiResponse("404", createApiResponse("Not Found"));
                    }
                    if (!apiResponses.containsKey("500")) {
                        apiResponses.addApiResponse("500", createApiResponse("Internal Server Error"));
                    }
                }));
    }

    private ApiResponse createApiResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", new MediaType()));
    }
}
