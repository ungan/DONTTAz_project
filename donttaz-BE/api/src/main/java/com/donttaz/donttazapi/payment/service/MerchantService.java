package com.donttaz.donttazapi.payment.service;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.payment.domain.Merchant;
import com.donttaz.donttazapi.payment.dto.request.CreateMerchantRequest;
import com.donttaz.donttazapi.payment.dto.response.MerchantResponse;
import com.donttaz.donttazapi.payment.repository.MerchantRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantResponse createMerchant(CreateMerchantRequest request) {
        if (merchantRepository.existsBySettlementAccountNo(request.settlementAccountNo())) {
            throw new CustomException(ErrorCode.MERCHANT_ALREADY_EXISTS);
        }

        Merchant merchant = merchantRepository.save(
                Merchant.builder()
                        .name(request.name())
                        .settlementAccountNo(request.settlementAccountNo())
                        .category(request.category())
                        .active(Boolean.TRUE)
                        .build()
        );

        return toMerchantResponse(merchant);
    }

    @Transactional(readOnly = true)
    public List<MerchantResponse> getMerchants() {
        return merchantRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(this::toMerchantResponse)
                .toList();
    }

    private MerchantResponse toMerchantResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .merchantId(merchant.getMerchantId())
                .name(merchant.getName())
                .settlementAccountNo(merchant.getSettlementAccountNo())
                .category(merchant.getCategory())
                .active(merchant.getActive())
                .build();
    }
}
