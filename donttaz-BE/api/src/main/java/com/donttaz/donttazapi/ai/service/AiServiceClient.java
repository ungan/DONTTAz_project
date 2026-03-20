package com.donttaz.donttazapi.ai.service;

import com.donttaz.donttazapi.ai.dto.request.AiAnalysisRequest;
import com.donttaz.donttazapi.ai.dto.response.AiAnalysisResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.donttaz.donttazapi.ai.dto.request.AiPlanAnalysisRequest;
import com.donttaz.donttazapi.ai.dto.response.AiPlanAnalysisResponse;

@Service
public class AiServiceClient {

    private final RestTemplate restTemplate;
    private final String aiServerUrl;
    private final String analysisEndpoint;
    private final String planningEndpoint;

    public AiServiceClient(
            RestTemplate restTemplate,
            @Value("${ai.server.url}") String aiServerUrl,
            @Value("${ai.analysis.endpoint:/api/v1/predict}") String analysisEndpoint,
            @Value("${ai.planning.endpoint:/api/v1/plan}") String planningEndpoint
    ) {
        this.restTemplate = restTemplate;
        this.aiServerUrl = aiServerUrl;
        this.analysisEndpoint = analysisEndpoint;
        this.planningEndpoint = planningEndpoint;
    }

    public AiAnalysisResponse analyzeMerchants(List<String> merchantNames) {
        String url = aiServerUrl + analysisEndpoint;
        AiAnalysisRequest request = new AiAnalysisRequest(merchantNames);
        return restTemplate.postForObject(url, request, AiAnalysisResponse.class);
    }

    public AiPlanAnalysisResponse calculateSpendingPlan(AiPlanAnalysisRequest request) {
        String url = aiServerUrl + planningEndpoint;
        return restTemplate.postForObject(url, request, AiPlanAnalysisResponse.class);
    }
}
