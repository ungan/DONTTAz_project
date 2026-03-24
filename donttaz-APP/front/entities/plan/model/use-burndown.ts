import { isAxiosError } from 'axios';
import { useEffect, useState } from 'react';
import { getBurndown } from '../api/get-burndown';
import type { BurndownData } from './types';

function isNoPlanError(error: unknown) {
  if (!isAxiosError(error)) {
    return false;
  }

  return error.response?.status === 400 && error.response?.data?.status === 'FAIL';
}

function getBurndownErrorMessage(error: unknown) {
  if (isAxiosError(error)) {
    return error.response?.data?.message ?? error.message ?? '버다운 데이터를 불러오지 못했습니다.';
  }

  if (error instanceof Error) {
    return error.message;
  }

  return '버다운 데이터를 불러오지 못했습니다.';
}

interface UseBurndownOptions {
  source?: string;
}

export function useBurndown(yearMonth: string, options?: UseBurndownOptions) {
  const source = options?.source ?? 'unknown';
  const [data, setData] = useState<BurndownData | null>(null);
  const [isPending, setIsPending] = useState(() => !!yearMonth);
  const [error, setError] = useState<string | null>(null);
  const [noPlan, setNoPlan] = useState(false);
  const [hasFetched, setHasFetched] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (!yearMonth) {
      return;
    }

    const fetch = async () => {
      setIsPending(true);
      setError(null);
      setNoPlan(false);

      try {
        const res = await getBurndown(yearMonth, source);
        setData(res.data ?? null);
      } catch (error) {
        const noPlanError = isNoPlanError(error);
        const message = getBurndownErrorMessage(error);

        if (noPlanError) {
          setData(null);
          setNoPlan(true);
          return;
        }

        setError(message);
      } finally {
        setIsPending(false);
        setHasFetched(true);
      }
    };

    void fetch();
  }, [yearMonth, reloadKey, source]);

  const reload = () => {
    setIsPending(true);
    setReloadKey((prev) => prev + 1);
  };

  return { data, isPending, error, noPlan, hasFetched, reload };
}
