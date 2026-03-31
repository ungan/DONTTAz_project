package com.donttaz.donttazapi.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiAnalysisResponse(
        String status,
        String message,
        @JsonAlias("categories")
        List<AiAnalysisResult> data
) {
    public AiAnalysisResponse(List<AiAnalysisResult> data) {
        this("SUCCESS", null, data);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AiAnalysisResult(
            String transactionId,
            String merchantName,
            String category,
            @JsonAlias("confidenceScore")
            Double confidence
    ) {
        public AiAnalysisResult(String merchantName, String category, Double confidence) {
            this(null, merchantName, category, confidence);
        }
    }

    // Keeps compatibility with existing callers that refer to results().
    public List<AiAnalysisResult> results() {
        return data;
    }
}
