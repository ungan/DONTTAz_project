package com.donttaz.donttazapi.memorandum.service;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import com.donttaz.donttazapi.memorandum.domain.Memorandum;
import com.donttaz.donttazapi.vault.domain.WishlistVault;
import com.donttaz.donttazapi.memorandum.dto.response.MemorandumResponse;
import com.donttaz.donttazapi.memorandum.repository.MemorandumRepository;
import com.donttaz.donttazapi.vault.repository.WishlistVaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemorandumService {

    private final MemorandumRepository memorandumRepository;
    private final WishlistVaultRepository wishlistVaultRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public void createInitialMemorandum(Long userId, Long wishlistVaultId, String c1, String c2, String c3) {
        Memorandum memorandum = Memorandum.builder()
                .userId(userId)
                .wishlistVaultId(wishlistVaultId)
                .content1(c1)
                .content2(c2)
                .content3(c3)
                .build();
        memorandumRepository.save(memorandum);
    }

    public MemorandumResponse getMemorandumByVaultId(Long wishlistVaultId) {
        Long userId = currentUserProvider.getCurrentUserId();
        Memorandum memorandum = memorandumRepository.findByWishlistVaultId(wishlistVaultId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "해당 금고의 각서를 찾을 수 없습니다."));

        if (!memorandum.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.VAULT_ACCESS_DENIED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        WishlistVault vault = wishlistVaultRepository.findById(wishlistVaultId)
                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

        return MemorandumResponse.builder()
                .id(memorandum.getId())
                .userId(user.getUserId())
                .userName(user.getName())
                .userPhoneNumber(user.getPhoneNumber())
                .wishlistVaultId(vault.getId())
                .wishName(vault.getWishName())
                .content1(memorandum.getContent1())
                .content2(memorandum.getContent2())
                .content3(memorandum.getContent3())
                .status(memorandum.getStatus())
                .signedAt(memorandum.getSignedAt())
                .build();
    }

    @Transactional
    public void signMemorandum(Long wishlistVaultId) {
        Long userId = currentUserProvider.getCurrentUserId();
        Memorandum memorandum = memorandumRepository.findByWishlistVaultId(wishlistVaultId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "각서를 찾을 수 없습니다."));

        if (!memorandum.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.VAULT_ACCESS_DENIED);
        }

        memorandum.sign();
    }

    @Transactional
    public void exposeByWithdraw(Long wishlistVaultId) {
        memorandumRepository.findByWishlistVaultId(wishlistVaultId)
                .ifPresent(Memorandum::exposeByWithdraw);
    }

    @Transactional
    public void exposeByTerminate(Long wishlistVaultId) {
        memorandumRepository.findByWishlistVaultId(wishlistVaultId)
                .ifPresent(Memorandum::exposeByTerminate);
    }
}
