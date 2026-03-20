package com.donttaz.donttazapi.ai.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "category_spending_plan")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategorySpendingPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_plan_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SpendingPlan spendingPlan;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    @Column(name = "allocated_amount", nullable = false)
    private Integer allocatedAmount;

    @Column(name = "ai_comment", length = 500)
    private String aiComment;

    @Builder
    public CategorySpendingPlan(
            String categoryName,
            Integer allocatedAmount,
            String aiComment
    ) {
        this.categoryName = categoryName;
        this.allocatedAmount = allocatedAmount;
        this.aiComment = aiComment;
    }

    protected void assignSpendingPlan(SpendingPlan spendingPlan) {
        this.spendingPlan = spendingPlan;
    }
}
