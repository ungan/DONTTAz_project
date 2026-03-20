package com.donttaz.donttazapi.ai.dto.request;

import java.util.List;

public record AiAnalysisRequest(
        List<String> merchantNames
) {
}
