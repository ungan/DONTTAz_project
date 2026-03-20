package com.donttaz.donttazapi.ai.service;

import com.donttaz.donttazapi.ai.dto.request.AiAnalysisRequest;
import com.donttaz.donttazapi.ai.dto.response.AiAnalysisResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiServiceClient {

    private final RestTemplate restTemplate;
    private final String aiServerUrl;
    private final String analysisEndpoint;

    public AiServiceClient(
            RestTemplate restTemplate,
            @Value("${ai.server.url}") String aiServerUrl,
            @Value("${ai.analysis.endpoint:/api/v1/predict}") String analysisEndpoint
    ) {
        this.restTemplate = restTemplate;
        this.aiServerUrl = aiServerUrl;
        this.analysisEndpoint = analysisEndpoint;
    }

    public AiAnalysisResponse analyzeMerchants(List<String> merchantNames) {
        String url = aiServerUrl + analysisEndpoint;
        AiAnalysisRequest request = new AiAnalysisRequest(merchantNames);
        return restTemplate.postForObject(url, request, AiAnalysisResponse.class);
    }
}
