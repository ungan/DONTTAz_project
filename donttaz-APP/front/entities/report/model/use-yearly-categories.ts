import { useState, useEffect } from 'react';
import { getMonthlyReport } from '../api/get-monthly-report';
import { CategorySummary } from './use-monthly-report';

const CATEGORY_COLORS: Record<string, string> = {
  '식비/카페': '#5899FD',
  '교통': '#FF6B6B',
  '문화/여가': '#FFD93D',
  '통신/보험': '#6BCB77',
  '쇼핑': '#C77DFF',
  '송금': '#FF9F43',
};

const DEFAULT_COLOR = '#A0AEC0';

const MONTHS = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12'];

function aggregateCategories(
  list: { aiCategory: string; amount: number }[]
): CategorySummary[] {
  const totals: Record<string, number> = {};

  for (const item of list) {
    totals[item.aiCategory] = (totals[item.aiCategory] ?? 0) + item.amount;
  }

  const grandTotal = Object.values(totals).reduce((sum, v) => sum + v, 0);
  const entries = Object.entries(totals).sort((a, b) => b[1] - a[1]);

  const floored = entries.map(([category, totalAmount]) => ({
    category,
    totalAmount,
    exact: grandTotal > 0 ? (totalAmount / grandTotal) * 100 : 0,
    color: CATEGORY_COLORS[category] ?? DEFAULT_COLOR,
    percentage: Math.floor(grandTotal > 0 ? (totalAmount / grandTotal) * 100 : 0),
  }));

  const remainder = 100 - floored.reduce((sum, item) => sum + item.percentage, 0);

  return floored
    .sort((a, b) => (b.exact - b.percentage) - (a.exact - a.percentage))
    .map((item, i) => ({
      category: item.category,
      totalAmount: item.totalAmount,
      percentage: item.percentage + (i < remainder ? 1 : 0),
      color: item.color,
    }));
}

export function useYearlyCategories(year: string) {
  const [categories, setCategories] = useState<CategorySummary[]>([]);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!year) return;

    const fetch = async () => {
      setIsPending(true);
      setError(null);
      try {
        const results = await Promise.all(
          MONTHS.map((month) => getMonthlyReport({ yearMonth: `${year}-${month}` }))
        );
        const allTransactions = results.flatMap((res) => res.data.categorizedList);
        const aggregated = aggregateCategories(allTransactions);
        setCategories(aggregated);
      } catch {
        setError('데이터를 불러오지 못했습니다.');
      } finally {
        setIsPending(false);
      }
    };

    fetch();
  }, [year]);

  return { categories, isPending, error };
}
