import type {
  BudgetStrategy,
  SpendingPlanCategoryPlan,
  SpendingPlanCategoryPlanResponse,
  SpendingPlanData,
  SpendingPlanRecommendation,
  SpendingPlanRecommendationResponse,
  SpendingPlanResponseData,
} from './plan-types';
import { getReportCategoryColor } from '@/entities/report';

const FALLBACK_COLORS = ['#F97316', '#FBBF24', '#5899FD', '#A855F7', '#10B981', '#94A3B8'];

function toInteger(value: number | undefined | null): number {
  return typeof value === 'number' && Number.isFinite(value) ? Math.max(0, Math.round(value)) : 0;
}

function mergeCategoryPlans(
  categoryPlans: SpendingPlanCategoryPlanResponse[],
  totalBudget: number
): SpendingPlanCategoryPlan[] {
  const merged = new Map<string, { allocatedAmount: number; aiComment: string }>();

  categoryPlans.forEach((plan) => {
    const name = plan.categoryName?.trim() || '기타';
    const nextComment = plan.aiComment?.trim() ?? '';
    const current = merged.get(name);

    if (current) {
      merged.set(name, {
        allocatedAmount: current.allocatedAmount + toInteger(plan.allocatedAmount),
        aiComment: current.aiComment || nextComment,
      });
      return;
    }

    merged.set(name, {
      allocatedAmount: toInteger(plan.allocatedAmount),
      aiComment: nextComment,
    });
  });

  const totalAllocatedAmount = Array.from(merged.values()).reduce(
    (sum, item) => sum + item.allocatedAmount,
    0
  );
  const divisor = totalBudget > 0 ? totalBudget : totalAllocatedAmount;

  const sorted = Array.from(merged.entries())
    .map(([categoryName, value], index) => ({
      categoryName,
      allocatedAmount: value.allocatedAmount,
      aiComment: value.aiComment || `${categoryName} 예산을 배정했습니다.`,
      color: getReportCategoryColor(categoryName) !== '#64748B'
        ? getReportCategoryColor(categoryName)
        : FALLBACK_COLORS[index % FALLBACK_COLORS.length],
      exactPercentage: divisor > 0 ? (value.allocatedAmount / divisor) * 100 : 0,
    }))
    .sort((a, b) => b.allocatedAmount - a.allocatedAmount);

  const floored = sorted.map((item) => ({
    ...item,
    percentage: Math.floor(item.exactPercentage),
    remainder: item.exactPercentage - Math.floor(item.exactPercentage),
  }));

  const allocatedPercentage = floored.reduce((sum, item) => sum + item.percentage, 0);
  const remainderCount = Math.max(0, 100 - allocatedPercentage);

  return floored
    .sort((a, b) => b.remainder - a.remainder)
    .map((item, index) => ({
      ...item,
      percentage: item.percentage + (index < remainderCount ? 1 : 0),
    }))
    .sort((a, b) => b.allocatedAmount - a.allocatedAmount)
    .map(({ categoryName, allocatedAmount, aiComment, percentage, color }) => ({
      categoryName,
      allocatedAmount,
      aiComment,
      percentage,
      color,
    }));
}

function toStrategyLabel(strategy: BudgetStrategy | null | undefined): string {
  switch (strategy) {
    case 'safe':
      return '안정형 전략';
    case 'balanced':
      return '균형형 전략';
    case 'tight':
    case 'aggressive':
      return '집중 관리형 전략';
    default:
      return '기본 전략';
  }
}

function toRiskLabel(riskScore: number | null): string {
  if (riskScore === null) {
    return '분석 전';
  }

  if (riskScore <= 0.3) {
    return '안정적';
  }

  if (riskScore <= 0.6) {
    return '주의 필요';
  }

  return '지출 관리 필요';
}

function toRecommendationStyle(type: string): Pick<SpendingPlanRecommendation, 'typeLabel' | 'accentColor' | 'backgroundColor'> {
  switch (type) {
    case 'warning':
      return {
        typeLabel: '주의',
        accentColor: '#E25B52',
        backgroundColor: '#FFF3F2',
      };
    case 'positive':
      return {
        typeLabel: '좋아요',
        accentColor: '#2B8A57',
        backgroundColor: '#EEF9F2',
      };
    case 'action':
      return {
        typeLabel: '실천',
        accentColor: '#3178F6',
        backgroundColor: '#EEF4FF',
      };
    case 'saving_tip':
      return {
        typeLabel: '절약 팁',
        accentColor: '#7F56D9',
        backgroundColor: '#F5F0FF',
      };
    case 'habit_change':
      return {
        typeLabel: '패턴 변화',
        accentColor: '#3178F6',
        backgroundColor: '#EEF4FF',
      };
    default:
      return {
        typeLabel: '안내',
        accentColor: '#5F6B7A',
        backgroundColor: '#F4F6F8',
      };
  }
}

function normalizeRecommendations(
  recommendations: SpendingPlanRecommendationResponse[] | undefined
): SpendingPlanRecommendation[] {
  return (recommendations ?? []).map((item) => ({
    ...item,
    ...toRecommendationStyle(item.type),
  }));
}

export function normalizeSpendingPlanData(data: SpendingPlanResponseData): SpendingPlanData {
  const totalBudget = toInteger(data.totalBudget);
  const riskScore = typeof data.riskScore === 'number' ? data.riskScore : null;
  const categoryPlans = mergeCategoryPlans(data.categoryPlans ?? [], totalBudget);

  return {
    planId: data.planId,
    targetYearMonth: data.targetYearMonth,
    referenceStartMonth: data.referenceStartMonth ?? null,
    referenceEndMonth: data.referenceEndMonth ?? null,
    totalBudget,
    sealedAmount: toInteger(data.sealedAmount),
    surplusAmount: toInteger(data.surplusAmount),
    paydayDay: data.paydayDay ?? null,
    riskScore,
    riskPercent: riskScore === null ? null : Math.round(riskScore * 100),
    riskLevelLabel: toRiskLabel(riskScore),
    budgetStrategy: data.budgetStrategy ?? null,
    budgetStrategyLabel: toStrategyLabel(data.budgetStrategy),
    spendingPersona: data.spendingPersona ?? null,
    categoryPlans,
    personalizedRecommendations: normalizeRecommendations(data.personalizedRecommendations),
    totalAllocatedAmount: categoryPlans.reduce((sum, item) => sum + item.allocatedAmount, 0),
  };
}
