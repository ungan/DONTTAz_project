package com.donttaz.donttazapi.memorandum.controller;

import com.donttaz.donttazapi.memorandum.dto.response.MemorandumResponse;
import com.donttaz.donttazapi.memorandum.service.MemorandumService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/memorandums")
@RequiredArgsConstructor
public class MemorandumController implements com.donttaz.donttazapi.memorandum.controller.docs.MemorandumSwagger {

    private final MemorandumService memorandumService;

    @Override
    @GetMapping("/{vaultId}")
    public MemorandumResponse getMemorandum(@PathVariable Long vaultId) {
        return memorandumService.getMemorandumByVaultId(vaultId);
    }

    @Override
    @PostMapping("/{vaultId}/sign")
    public void signMemorandum(@PathVariable Long vaultId) {
        memorandumService.signMemorandum(vaultId);
    }
}
