import { useState, useCallback } from 'react';
import { useBankAccountStore } from '@/entities/bank-account';
import { getPaymentsApi } from '../api/get-payments';
import type { Transaction } from '../types/payment';

export function usePaymentsInfinite() {
  const { accountId } = useBankAccountStore();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [page, setPage] = useState(0);
  const [isLast, setIsLast] = useState(false);
  const [isPending, setIsPending] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchPage = useCallback(
    async (pageToFetch: number, replace: boolean) => {
      if (!accountId) return;

      replace ? setIsRefreshing(true) : setIsPending(true);
      setError(null);

      try {
        const res = await getPaymentsApi({ accountId, page: pageToFetch, size: 20 });
        setTransactions(prev => (replace ? res.data.transactions : [...prev, ...res.data.transactions]));
        setIsLast(res.data.isLast);
        setPage(pageToFetch);
      } catch {
        setError('거래내역을 불러오지 못했습니다.');
      } finally {
        setIsPending(false);
        setIsRefreshing(false);
      }
    },
    [accountId]
  );

  const loadFirst = useCallback(() => {
    void fetchPage(0, true);
  }, [fetchPage]);

  const loadNext = useCallback(() => {
    if (isPending || isRefreshing || isLast) return;
    void fetchPage(page + 1, false);
  }, [fetchPage, isPending, isRefreshing, isLast, page]);

  return { transactions, isPending, isRefreshing, isLast, error, loadFirst, loadNext };
}
