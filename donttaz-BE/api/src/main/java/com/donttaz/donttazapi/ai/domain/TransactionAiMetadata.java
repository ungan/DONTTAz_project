package com.donttaz.donttazapi.ai.domain;

import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "transaction_ai_metadata")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionAiMetadata extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_tx_id", nullable = false)
    private Transaction transaction;

    @Column(name = "ai_category", length = 50)
    private String aiCategory;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "is_modified", nullable = false)
    private Boolean isModified;

    @Column(name = "modified_category", length = 50)
    private String modifiedCategory;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Builder
    public TransactionAiMetadata(
            Transaction transaction,
            String aiCategory,
            Double confidenceScore,
            Boolean isModified,
            String modifiedCategory,
            LocalDateTime modifiedAt
    ) {
        this.transaction = transaction;
        this.aiCategory = aiCategory;
        this.confidenceScore = confidenceScore;
        this.isModified = isModified != null ? isModified : false;
        this.modifiedCategory = modifiedCategory;
        this.modifiedAt = modifiedAt;
    }

    public void updateCategory(String category) {
        this.modifiedCategory = category;
        this.isModified = true;
        this.modifiedAt = LocalDateTime.now();
    }

    public void applyAiAnalysis(String category, Double confidenceScore) {
        this.aiCategory = category;
        this.confidenceScore = confidenceScore;
    }
}
