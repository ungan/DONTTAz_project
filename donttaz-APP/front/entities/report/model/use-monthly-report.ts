import { useState, useEffect } from 'react';
import { getMonthlyReport } from '../api/get-monthly-report';
import {
  MonthlyReportResponse,
  SpendingReportData,
  SpendingReportFeedbackItem,
} from '../types/report';
import { getReportCategoryColor } from './report-category-colors';

export interface CategorySummary {
  category: string;
  totalAmount: number;
  percentage: number;
  allocatedAmount: number;
  budgetAchievementRate: number | null;
  feedback: string;
  color: string;
}

export type MonthlyReportViewState = 'ready' | 'empty' | 'no-plan' | 'error';

export interface MonthlyReportSummary {
  yearMonth: string;
  sealedAmount: number;
  totalBudget: number;
  totalSpent: number;
  achievementRate: number;
  spendingPersona: string | null;
  aiOverallFeedback: string;
  personalizedFeedback: SpendingReportFeedbackItem[];
}

function toBudgetAchievementRate(
  spentAmount: number,
  allocatedAmount: number,
  responseRate?: number
): number | null {
  if (typeof responseRate === 'number') {
    return responseRate;
  }
  if (allocatedAmount <= 0) {
    return null;
  }
  return Number(((spentAmount / allocatedAmount) * 100).toFixed(1));
}

function toCategorySummary(data: SpendingReportData): CategorySummary[] {
  return data.categoryReports.map((item) => ({
    category: item.categoryName,
    totalAmount: item.spentAmount,
    percentage: item.categoryRate,
    allocatedAmount: item.allocatedAmount,
    budgetAchievementRate: toBudgetAchievementRate(
      item.spentAmount,
      item.allocatedAmount,
      item.budgetAchievementRate
    ),
    feedback: item.feedback,
    color: getReportCategoryColor(item.categoryName),
  }));
}

function toSummary(data: SpendingReportData): MonthlyReportSummary {
  return {
    yearMonth: data.yearMonth,
    sealedAmount: data.sealedAmount,
    totalBudget: data.totalBudget,
    totalSpent: data.totalSpent,
    achievementRate: data.achievementRate,
    spendingPersona: data.spendingPersona,
    aiOverallFeedback: data.aiOverallFeedback,
    personalizedFeedback: data.personalizedFeedback,
  };
}

function buildFallbackSummary(yearMonth: string): MonthlyReportSummary {
  return {
    yearMonth,
    sealedAmount: 0,
    totalBudget: 0,
    totalSpent: 0,
    achievementRate: 0,
    spendingPersona: null,
    aiOverallFeedback: '',
    personalizedFeedback: [],
  };
}

function isNoPlanMessage(message: string) {
  return new RegExp('\\uD50C\\uB79C|\\uACC4\\uD68D|\\uC608\\uC0B0', 'i').test(message);
}

function resolveViewState(
  summary: MonthlyReportSummary,
  categories: CategorySummary[]
): MonthlyReportViewState {
  const hasSpending = summary.totalSpent > 0;
  const hasCategoryAmount = categories.some((item) => item.totalAmount > 0);
  return hasSpending || hasCategoryAmount ? 'ready' : 'empty';
}

export function useMonthlyReport(yearMonth: string) {
  const [summary, setSummary] = useState<MonthlyReportSummary>(buildFallbackSummary(yearMonth));
  const [categories, setCategories] = useState<CategorySummary[]>([]);
  const [viewState, setViewState] = useState<MonthlyReportViewState>('ready');
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!yearMonth) return;

    const fetch = async () => {
      setIsPending(true);
      setError(null);
      setStatusMessage(null);

      try {
        const res: MonthlyReportResponse = await getMonthlyReport({ yearMonth });

        if (res.status !== 'SUCCESS' || !res.data) {
          const message =
            res.message || '\uB9AC\uD3EC\uD2B8 \uB370\uC774\uD130\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4.';
          setSummary(buildFallbackSummary(yearMonth));
          setCategories([]);
          setStatusMessage(message);
          setViewState(isNoPlanMessage(message) ? 'no-plan' : 'error');
          return;
        }

        const nextSummary = toSummary(res.data);
        const nextCategories = toCategorySummary(res.data);
        setSummary(nextSummary);
        setCategories(nextCategories);
        setStatusMessage(res.message);
        setViewState(resolveViewState(nextSummary, nextCategories));
      } catch {
        setSummary(buildFallbackSummary(yearMonth));
        setCategories([]);
        setStatusMessage(
          '\uB124\uD2B8\uC6CC\uD06C \uC0C1\uD0DC\uB97C \uD655\uC778\uD55C \uB4A4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.'
        );
        setViewState('error');
        setError('\uB370\uC774\uD130\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4.');
      } finally {
        setIsPending(false);
      }
    };

    fetch();
  }, [yearMonth]);

  const topCategory = categories[0] ?? null;
  const budgetGap = summary.totalBudget - summary.totalSpent;

  return {
    summary,
    categories,
    topCategory,
    budgetGap,
    viewState,
    statusMessage,
    isPending,
    error,
  };
}
