import { useState, useEffect } from 'react';
import { getYearlyReport } from '../api/get-yearly-report';

export interface MonthlyExpenditure {
  month: string; // '01' ~ '12'
  amount: number;
}

export function useYearlyReport(year: string) {
  const [monthlyData, setMonthlyData] = useState<MonthlyExpenditure[]>([]);
  const [totalAmount, setTotalAmount] = useState(0);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!year) return;

    const fetch = async () => {
      setIsPending(true);
      setError(null);
      try {
        const res = await getYearlyReport();
        const yearData = res.data[year] ?? {};
        const data: MonthlyExpenditure[] = Object.entries(yearData)
          .sort(([a], [b]) => a.localeCompare(b))
          .map(([month, amount]) => ({ month, amount }));
        const total = data.reduce((sum, d) => sum + d.amount, 0);
        setMonthlyData(data);
        setTotalAmount(total);
      } catch {
        setError('데이터를 불러오지 못했습니다.');
      } finally {
        setIsPending(false);
      }
    };

    fetch();
  }, [year]);

  return { monthlyData, totalAmount, isPending, error };
}
