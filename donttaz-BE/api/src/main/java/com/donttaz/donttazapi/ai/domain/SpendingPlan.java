package com.donttaz.donttazapi.ai.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "spending_plan")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpendingPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_year_month", nullable = false, length = 7)
    private String targetYearMonth;

    @Column(name = "monthly_income", nullable = false)
    private Integer monthlyIncome;

    @Column(name = "total_budget", nullable = false)
    private Integer totalBudget;

    @Column(name = "surplus_amount", nullable = false)
    private Integer surplusAmount;

    @OneToMany(mappedBy = "spendingPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategorySpendingPlan> categoryPlans = new ArrayList<>();

    @Builder
    public SpendingPlan(
            Long userId,
            String targetYearMonth,
            Integer monthlyIncome,
            Integer totalBudget,
            Integer surplusAmount
    ) {
        this.userId = userId;
        this.targetYearMonth = targetYearMonth;
        this.monthlyIncome = monthlyIncome;
        this.totalBudget = totalBudget;
        this.surplusAmount = surplusAmount;
    }

    public void addCategoryPlan(CategorySpendingPlan categoryPlan) {
        this.categoryPlans.add(categoryPlan);
        categoryPlan.assignSpendingPlan(this);
    }
}
