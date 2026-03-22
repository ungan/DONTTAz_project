package com.donttaz.donttazapi.webhook.controller;

import com.donttaz.donttazapi.webhook.controller.docs.PenaltyWebhookSwagger;
import com.donttaz.donttazapi.webhook.dto.request.PenaltyWebhookRequest;
import com.donttaz.donttazapi.webhook.dto.response.PenaltyWebhookResponse;
import com.donttaz.donttazapi.webhook.service.PenaltyWebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/penalty-webhooks")
@RequiredArgsConstructor
public class PenaltyWebhookController implements PenaltyWebhookSwagger {

    private final PenaltyWebhookService penaltyWebhookService;

    @Override
    @PostMapping("/{vaultId}")
    public PenaltyWebhookResponse saveOrUpdateWebhook(
            @PathVariable Long vaultId,
            @RequestBody @Valid PenaltyWebhookRequest request) {
        return penaltyWebhookService.saveOrUpdateWebhook(vaultId, request);
    }

    @Override
    @GetMapping("/{vaultId}")
    public PenaltyWebhookResponse getWebhook(@PathVariable Long vaultId) {
        return penaltyWebhookService.getWebhookByVaultId(vaultId);
    }
}
