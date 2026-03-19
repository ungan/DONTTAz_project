import { useState, useEffect } from 'react';
import { getBurndown } from '../api/get-burndown';
import type { BurndownData } from './types';

export function useBurndown(yearMonth: string) {
  const [data, setData] = useState<BurndownData | null>(null);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!yearMonth) return;

    const fetch = async () => {
      setIsPending(true);
      setError(null);
      try {
        const res = await getBurndown(yearMonth);
        setData(res.data);
      } catch {
        setError('데이터를 불러오지 못했습니다.');
      } finally {
        setIsPending(false);
      }
    };

    fetch();
  }, [yearMonth]);

  return { data, isPending, error };
}
