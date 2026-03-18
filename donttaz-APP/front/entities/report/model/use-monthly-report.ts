import { useState, useEffect } from 'react';
import { getMonthlyReport } from '../api/get-monthly-report';

export interface CategorySummary {
  category: string;
  totalAmount: number;
  percentage: number;
  color: string;
}

const CATEGORY_COLORS: Record<string, string> = {
  '식비/카페': '#5899FD',
  '교통': '#FF6B6B',
  '문화/여가': '#FFD93D',
  '통신/보험': '#6BCB77',
  '쇼핑': '#C77DFF',
  '송금': '#FF9F43',
};

const DEFAULT_COLOR = '#A0AEC0';

function aggregateByCategory(
  list: { aiCategory: string; amount: number }[]
): CategorySummary[] {
  const totals: Record<string, number> = {};

  for (const item of list) {
    totals[item.aiCategory] = (totals[item.aiCategory] ?? 0) + item.amount;
  }

  const grandTotal = Object.values(totals).reduce((sum, v) => sum + v, 0);

  const entries = Object.entries(totals).sort((a, b) => b[1] - a[1]);

  const exact = entries.map(([category, totalAmount]) => ({
    category,
    totalAmount,
    exact: grandTotal > 0 ? (totalAmount / grandTotal) * 100 : 0,
    color: CATEGORY_COLORS[category] ?? DEFAULT_COLOR,
  }));

  const floored = exact.map((item) => ({ ...item, percentage: Math.floor(item.exact) }));
  const remainder = 100 - floored.reduce((sum, item) => sum + item.percentage, 0);

  const result = floored
    .map((item, i) => ({ ...item, remainder: item.exact - item.percentage, index: i }))
    .sort((a, b) => b.remainder - a.remainder)
    .map((item, i) => ({ ...item, percentage: item.percentage + (i < remainder ? 1 : 0) }))
    .sort((a, b) => a.index - b.index)
    .map(({ category, totalAmount, percentage, color }) => ({ category, totalAmount, percentage, color }));

  return result;
}

export function useMonthlyReport(yearMonth: string) {
  const [categories, setCategories] = useState<CategorySummary[]>([]);
  const [totalAmount, setTotalAmount] = useState(0);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!yearMonth) return;

    const fetch = async () => {
      setIsPending(true);
      setError(null);
      try {
        const res = await getMonthlyReport({ yearMonth });
        const list = res.data.categorizedList;
        const aggregated = aggregateByCategory(list);
        const total = aggregated.reduce((sum, c) => sum + c.totalAmount, 0);
        setCategories(aggregated);
        setTotalAmount(total);
      } catch {
        setError('데이터를 불러오지 못했습니다.');
      } finally {
        setIsPending(false);
      }
    };

    fetch();
  }, [yearMonth]);

  return { categories, totalAmount, isPending, error };
}
