import { useState, useEffect } from 'react';
import { getMonthlyReport } from '../api/get-monthly-report';
import { CategorySummary } from './use-monthly-report';
import { getReportCategoryColor } from './report-category-colors';

const MONTHS = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12'];

function aggregateCategories(
  list: { categoryName: string; spentAmount: number }[]
): CategorySummary[] {
  const totals: Record<string, number> = {};

  for (const item of list) {
    totals[item.categoryName] = (totals[item.categoryName] ?? 0) + item.spentAmount;
  }

  const grandTotal = Object.values(totals).reduce((sum, value) => sum + value, 0);

  return Object.entries(totals)
    .sort((a, b) => b[1] - a[1])
    .map(([category, totalAmount]) => ({
      category,
      totalAmount,
      percentage: grandTotal > 0 ? Number(((totalAmount / grandTotal) * 100).toFixed(1)) : 0,
      allocatedAmount: 0,
      budgetAchievementRate: null,
      feedback: '',
      color: getReportCategoryColor(category),
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
        const allCategoryReports = results.flatMap((res) => res.data?.categoryReports ?? []);
        setCategories(aggregateCategories(allCategoryReports));
      } catch {
        setError('\uC5F0\uAC04 \uB9AC\uD3EC\uD2B8\uB294 \uC900\uBE44 \uC911\uC785\uB2C8\uB2E4.');
      } finally {
        setIsPending(false);
      }
    };

    fetch();
  }, [year]);

  return { categories, isPending, error };
}
