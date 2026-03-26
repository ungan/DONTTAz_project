import { isAxiosError } from 'axios';
import { apiClient } from '@/shared/api';
import { getFallbackSpendingPlanResponse } from '../model/spending-plan-fallback';
import type { SpendingPlanResponse } from '../model/plan-types';

interface LegacySpendingPlanResponse {
  planId?: number;
  targetYearMonth: string;
  totalBudget: number;
  surplusAmount?: number;
  categoryPlans: Array<{
    categoryName: string;
    allocatedAmount: number;
    aiComment?: string | null;
  }>;
}

interface CreateSpendingPlanParams {
  targetYearMonth: string;
  monthlyIncome?: number;
}

function logPlanApi(event: string, payload: Record<string, unknown>) {
  if (!__DEV__) {
    return;
  }

  console.log(`[PlanDebug][API] ${event}`, payload);
}

function isSpendingPlanResponse(value: unknown): value is SpendingPlanResponse {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const response = value as Partial<SpendingPlanResponse>;
  return !!response.data && typeof response.data === 'object' && Array.isArray(response.data.categoryPlans);
}

function isLegacySpendingPlanResponse(value: unknown): value is LegacySpendingPlanResponse {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const response = value as Partial<LegacySpendingPlanResponse>;
  return typeof response.targetYearMonth === 'string' && Array.isArray(response.categoryPlans);
}

function adaptLegacySpendingPlanResponse(response: LegacySpendingPlanResponse): SpendingPlanResponse {
  return {
    status: 'SUCCESS',
    message: '플랜 생성 완료',
    data: {
      planId: response.planId,
      targetYearMonth: response.targetYearMonth,
      referenceStartMonth: null,
      referenceEndMonth: null,
      totalBudget: response.totalBudget,
      sealedAmount: 0,
      surplusAmount: response.surplusAmount ?? 0,
      paydayDay: null,
      riskScore: null,
      budgetStrategy: null,
      categoryPlans: response.categoryPlans,
      personalizedRecommendations: [],
      spendingPersona: null,
    },
  };
}

export async function createSpendingPlan({
  targetYearMonth,
  monthlyIncome,
}: CreateSpendingPlanParams): Promise<SpendingPlanResponse> {
  const payload: CreateSpendingPlanParams = {
    targetYearMonth,
  };

  if (typeof monthlyIncome === 'number') {
    payload.monthlyIncome = monthlyIncome;
  }

  logPlanApi('request:start', payload);

  try {
    const { data } = await apiClient.post<SpendingPlanResponse | LegacySpendingPlanResponse>(
      '/ai/plans',
      payload
    );

    if (isSpendingPlanResponse(data)) {
      logPlanApi('request:success', {
        shape: 'standard',
        targetYearMonth: data.data.targetYearMonth,
        totalBudget: data.data.totalBudget,
        categoryPlansCount: data.data.categoryPlans.length,
      });
      return data;
    }

    if (isLegacySpendingPlanResponse(data)) {
      logPlanApi('request:success', {
        shape: 'legacy',
        targetYearMonth: data.targetYearMonth,
        totalBudget: data.totalBudget,
        categoryPlansCount: data.categoryPlans.length,
      });
      return adaptLegacySpendingPlanResponse(data);
    }

    logPlanApi('request:fallback', { reason: 'unknown-response-shape', targetYearMonth });
    return getFallbackSpendingPlanResponse(targetYearMonth);
  } catch (error) {
    if (isAxiosError(error)) {
      console.warn('[Plan] createSpendingPlan fallback activated', {
        status: error.response?.status,
        message: error.message,
      });
    }

    logPlanApi('request:fallback', {
      reason: 'request-error',
      targetYearMonth,
      error: error instanceof Error ? error.message : 'unknown',
    });

    return getFallbackSpendingPlanResponse(targetYearMonth);
  }
}
