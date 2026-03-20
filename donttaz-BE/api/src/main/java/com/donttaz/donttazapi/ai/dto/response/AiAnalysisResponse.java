package com.donttaz.donttazapi.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AiAnalysisResponse(
        List<AiAnalysisResult> results
) {
    public record AiAnalysisResult(
            String merchantName,
            String category,
            Double confidence
    ) {
    }
}
