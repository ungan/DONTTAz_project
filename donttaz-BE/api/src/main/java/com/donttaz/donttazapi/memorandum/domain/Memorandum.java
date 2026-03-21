package com.donttaz.donttazapi.memorandum.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "memorandum")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Memorandum extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memorandum_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wishlist_vault_id", nullable = false)
    private Long wishlistVaultId;

    @Column(name = "content1", length = 500)
    private String content1; // 제1조: 사인 규명 (사전 고백)

    @Column(name = "content2", length = 500)
    private String content2; // 제2조: 주변 사람 공표 사항

    @Column(name = "content3", length = 500)
    private String content3; // 제3조: 향후 다짐

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemorandumStatus status;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Builder
    private Memorandum(Long userId, Long wishlistVaultId, String content1, String content2, String content3) {
        this.userId = userId;
        this.wishlistVaultId = wishlistVaultId;
        this.content1 = content1;
        this.content2 = content2;
        this.content3 = content3;
        this.status = MemorandumStatus.WRITING;
    }

    public void sign() {
        if (this.status != MemorandumStatus.WRITING) {
            throw new IllegalStateException("작성 중인 각서만 서명할 수 있습니다.");
        }
        this.status = MemorandumStatus.SIGNED;
        this.signedAt = LocalDateTime.now();
    }

    public void exposeByWithdraw() {
        if (this.status == MemorandumStatus.SIGNED) {
            this.status = MemorandumStatus.EXPOSED_WITHDRAW;
        }
    }

    public void exposeByTerminate() {
        if (this.status == MemorandumStatus.SIGNED || this.status == MemorandumStatus.EXPOSED_WITHDRAW) {
            this.status = MemorandumStatus.EXPOSED_TERMINATE;
        }
    }

    public void updateContent(String content1, String content2, String content3) {
        if (this.status != MemorandumStatus.WRITING) {
            throw new IllegalStateException("서명된 각서는 수정할 수 없습니다.");
        }
        this.content1 = content1;
        this.content2 = content2;
        this.content3 = content3;
    }
}
