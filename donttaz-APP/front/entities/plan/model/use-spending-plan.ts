import { isAxiosError } from 'axios';
import { useCallback, useEffect, useState } from 'react';
import { createSpendingPlan } from '../api/create-spending-plan';
import type { SpendingPlanData } from './plan-types';
import { normalizeSpendingPlanData } from './spending-plan-utils';

function getSpendingPlanErrorMessage(error: unknown) {
  if (isAxiosError(error)) {
    return error.response?.data?.message ?? error.message ?? '소비 플랜을 불러오지 못했어요.';
  }

  if (error instanceof Error) {
    return error.message;
  }

  return '소비 플랜을 불러오지 못했어요.';
}

interface UseSpendingPlanOptions {
  monthlyIncome?: number;
  source?: string;
}

function logPlanHook(event: string, payload: Record<string, unknown>) {
  if (!__DEV__) {
    return;
  }

  console.log(`[PlanDebug][Hook] ${event}`, payload);
}

export function useSpendingPlan(yearMonth: string, options?: UseSpendingPlanOptions) {
  const [data, setData] = useState<SpendingPlanData | null>(null);
  const [isPending, setIsPending] = useState(() => !!yearMonth);
  const [error, setError] = useState<string | null>(null);
  const [hasFetched, setHasFetched] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (!yearMonth) {
      return;
    }

    const fetch = async () => {
      setIsPending(true);
      setError(null);
      logPlanHook('fetch:start', {
        yearMonth,
        source: options?.source ?? 'unknown',
        monthlyIncome: options?.monthlyIncome ?? null,
      });

      try {
        const response = await createSpendingPlan({
          targetYearMonth: yearMonth,
          monthlyIncome: options?.monthlyIncome,
        });
        const normalized = normalizeSpendingPlanData(response.data);
        setData(normalized);
        logPlanHook('fetch:success', {
          yearMonth,
          totalBudget: normalized.totalBudget,
          sealedAmount: normalized.sealedAmount,
          categoryPlansCount: normalized.categoryPlans.length,
          recommendationsCount: normalized.personalizedRecommendations.length,
        });
      } catch (error) {
        setData(null);
        setError(getSpendingPlanErrorMessage(error));
        logPlanHook('fetch:error', {
          yearMonth,
          error: getSpendingPlanErrorMessage(error),
        });
      } finally {
        setIsPending(false);
        setHasFetched(true);
      }
    };

    void fetch();
  }, [yearMonth, reloadKey, options?.monthlyIncome, options?.source]);

  const reload = useCallback(() => {
    setIsPending(true);
    setReloadKey((prev) => prev + 1);
  }, []);

  return { data, isPending, error, hasFetched, reload };
}
