package com.donttaz.donttazapi.ai.service;

import com.donttaz.donttazapi.ai.dto.request.AiAnalysisRequest;
import com.donttaz.donttazapi.ai.dto.request.AiPlanAnalysisRequest;
import com.donttaz.donttazapi.ai.dto.request.AiSpendingReportGenerateRequest;
import com.donttaz.donttazapi.ai.dto.response.AiAnalysisResponse;
import com.donttaz.donttazapi.ai.dto.response.AiPlanAnalysisResponse;
import com.donttaz.donttazapi.ai.dto.response.AiSpendingReportGenerateResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AiServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String aiServerUrl;
    private final String analysisEndpoint;
    private final String planningEndpoint;
    private final String reportEndpoint;

    public AiServiceClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${ai.server.url}") String aiServerUrl,
            @Value("${ai.analysis.endpoint:/internal/ai/categories/classify}") String analysisEndpoint,
            @Value("${ai.planning.endpoint:/internal/ai/plans/generate}") String planningEndpoint,
            @Value("${ai.report.endpoint:/internal/ai/reports/generate}") String reportEndpoint
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.aiServerUrl = aiServerUrl;
        this.analysisEndpoint = analysisEndpoint;
        this.planningEndpoint = planningEndpoint;
        this.reportEndpoint = reportEndpoint;
    }

    public AiAnalysisResponse analyzeTransactions(AiAnalysisRequest request) {
        String url = aiServerUrl + analysisEndpoint;
        log.info("[AI Request] Calling analysis API: {}", url);
        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(request), String.class);
        AiAnalysisResponse parsedResponse = readResponse(response, AiAnalysisResponse.class, "analysis");

        log.info(
                "[AI Response] Analysis parsed status={}, message={}, resultCount={}",
                parsedResponse != null ? parsedResponse.status() : "null",
                parsedResponse != null ? parsedResponse.message() : "null",
                parsedResponse != null && parsedResponse.data() != null ? parsedResponse.data().size() : null
        );
        return parsedResponse;
    }

    public AiAnalysisResponse analyzeMerchants(AiAnalysisRequest request) {
        return analyzeTransactions(request);
    }

    public AiPlanAnalysisResponse calculateSpendingPlan(AiPlanAnalysisRequest request) {
        String url = aiServerUrl + planningEndpoint;
        log.info("[AI Request] Calling planning API: {}", url);
        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(request), String.class);
        AiPlanAnalysisResponse parsedResponse = readResponse(response, AiPlanAnalysisResponse.class, "planning");

        log.info(
                "[AI Response] Planning parsed status={}, message={}, hasData={}, categoryPlanCount={}",
                parsedResponse != null ? parsedResponse.status() : "null",
                parsedResponse != null ? parsedResponse.message() : "null",
                parsedResponse != null && parsedResponse.data() != null,
                parsedResponse != null && parsedResponse.data() != null && parsedResponse.data().categories() != null
                        ? parsedResponse.data().categories().size()
                        : null
        );

        if (parsedResponse == null || !"SUCCESS".equals(parsedResponse.status()) || parsedResponse.data() == null) {
            log.error("[AI Error] Invalid response from AI server. Response: {}", parsedResponse);
            throw new RuntimeException("AI 서버로부터 올바른 소비 계획 응답을 받지 못했습니다.");
        }

        return parsedResponse;
    }

    public AiSpendingReportGenerateResponse calculateSpendingReport(AiSpendingReportGenerateRequest request) {
        String url = aiServerUrl + reportEndpoint;
        log.info("[AI Request] Calling report API: {}", url);
        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(request), String.class);
        AiSpendingReportGenerateResponse parsedResponse =
                readResponse(response, AiSpendingReportGenerateResponse.class, "report");

        log.info(
                "[AI Response] Report parsed status={}, message={}, hasData={}, categoryReportCount={}",
                parsedResponse != null ? parsedResponse.status() : "null",
                parsedResponse != null ? parsedResponse.message() : "null",
                parsedResponse != null && parsedResponse.data() != null,
                parsedResponse != null && parsedResponse.data() != null && parsedResponse.data().categoryReports() != null
                        ? parsedResponse.data().categoryReports().size()
                        : null
        );

        if (parsedResponse == null || !"SUCCESS".equals(parsedResponse.status()) || parsedResponse.data() == null) {
            log.error("[AI Error] Invalid report response from AI server. Response: {}", parsedResponse);
            throw new RuntimeException("AI 서버로부터 올바른 소비 리포트 응답을 받지 못했습니다.");
        }

        return parsedResponse;
    }

    private <T> T readResponse(ResponseEntity<String> response, Class<T> responseType, String context) {
        log.info("[AI Response] {} status: {}", context, response.getStatusCode());
        log.info("[AI Response] {} raw body: {}", context, response.getBody());

        try {
            return objectMapper.readValue(response.getBody(), responseType);
        } catch (JsonProcessingException exception) {
            log.error("[AI Response] Failed to parse {} response body: {}", context, response.getBody(), exception);
            throw new RuntimeException("Failed to parse AI " + context + " response", exception);
        }
    }
}
