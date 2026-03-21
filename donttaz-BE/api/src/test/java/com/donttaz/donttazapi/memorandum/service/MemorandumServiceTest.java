package com.donttaz.donttazapi.memorandum.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.memorandum.domain.Memorandum;
import com.donttaz.donttazapi.memorandum.domain.MemorandumStatus;
import com.donttaz.donttazapi.memorandum.dto.response.MemorandumResponse;
import com.donttaz.donttazapi.memorandum.repository.MemorandumRepository;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import com.donttaz.donttazapi.vault.domain.WishlistVault;
import com.donttaz.donttazapi.vault.repository.WishlistVaultRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemorandumServiceTest {

    @Mock
    private MemorandumRepository memorandumRepository;

    @Mock
    private WishlistVaultRepository wishlistVaultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private MemorandumService memorandumService;

    @Test
    @DisplayName("각서 초기 생성 시 정상적으로 저장되어야 한다.")
    void createInitialMemorandum_Success() {
        // Given
        Long userId = 1L;
        Long vaultId = 10L;

        // When
        memorandumService.createInitialMemorandum(userId, vaultId, "사인", "공표", "다짐");

        // Then
        verify(memorandumRepository).save(any(Memorandum.class));
    }

    @Test
    @DisplayName("정상적인 권한으로 각서를 조회하면 상세 정보를 반환한다.")
    void getMemorandumByVaultId_Success() {
        // Given
        Long userId = 1L;
        Long vaultId = 10L;

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);

        Memorandum mockMemo = Memorandum.builder()
                .userId(userId)
                .wishlistVaultId(vaultId)
                .content1("사인")
                .content2("공표")
                .content3("다짐")
                .build();
        ReflectionTestUtils.setField(mockMemo, "id", 100L);
        given(memorandumRepository.findByWishlistVaultId(vaultId)).willReturn(Optional.of(mockMemo));

        User mockUser = User.builder()
                .email("test@test.com")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();
        ReflectionTestUtils.setField(mockUser, "userId", userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        WishlistVault mockVault = WishlistVault.builder()
                .userId(userId)
                .wishName("애플워치")
                .goalAmount(500000L)
                .build();
        ReflectionTestUtils.setField(mockVault, "id", vaultId);
        given(wishlistVaultRepository.findById(vaultId)).willReturn(Optional.of(mockVault));

        // When
        MemorandumResponse result = memorandumService.getMemorandumByVaultId(vaultId);

        // Then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.userName()).isEqualTo("홍길동");
        assertThat(result.wishName()).isEqualTo("애플워치");
        assertThat(result.content1()).isEqualTo("사인");
        assertThat(result.status()).isEqualTo(MemorandumStatus.WRITING);
    }

    @Test
    @DisplayName("다른 사용자의 각서를 조회하려고 하면 예외가 발생한다.")
    void getMemorandumByVaultId_AccessDenied_ThrowsException() {
        // Given
        Long myId = 1L;
        Long otherId = 2L;
        Long vaultId = 10L;

        given(currentUserProvider.getCurrentUserId()).willReturn(myId);

        Memorandum otherMemo = Memorandum.builder()
                .userId(otherId) // 소유자는 2번 유저
                .wishlistVaultId(vaultId)
                .build();
        given(memorandumRepository.findByWishlistVaultId(vaultId)).willReturn(Optional.of(otherMemo));

        // When & Then
        assertThrows(CustomException.class, () -> memorandumService.getMemorandumByVaultId(vaultId));
    }

    @Test
    @DisplayName("작성 중인 각서에 서명하면 상태가 변경되어야 한다.")
    void signMemorandum_Success() {
        // Given
        Long userId = 1L;
        Long vaultId = 10L;

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);

        Memorandum mockMemo = Memorandum.builder()
                .userId(userId)
                .wishlistVaultId(vaultId)
                .build(); // 기본 상태는 WRITING
        given(memorandumRepository.findByWishlistVaultId(vaultId)).willReturn(Optional.of(mockMemo));

        // When
        memorandumService.signMemorandum(vaultId);

        // Then
        assertThat(mockMemo.getStatus()).isEqualTo(MemorandumStatus.SIGNED);
        assertThat(mockMemo.getSignedAt()).isNotNull();
    }
}
