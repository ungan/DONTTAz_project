import { useState, useEffect, useCallback } from 'react';
import { getVaultApi } from '../api/get-vault';
import { getVaultTransactionsApi } from '../api/get-vault-transactions';
import type { Vault, VaultTransaction } from '../types/vault';

export function useVault() {
  const [vault, setVault] = useState<Vault | null>(null);
  const [transactions, setTransactions] = useState<VaultTransaction[]>([]);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchVault = useCallback(async () => {
    setIsPending(true);
    setError(null);
    try {
      const res = await getVaultApi();
      const current = res.vaults[0] ?? null;
      setVault(current);
      if (current) {
        const txRes = await getVaultTransactionsApi(current.vaultId);
        setTransactions(txRes.data);
      }
    } catch {
      setError('가상금고 정보를 불러오지 못했습니다.');
    } finally {
      setIsPending(false);
    }
  }, []);

  useEffect(() => {
    fetchVault();
  }, [fetchVault]);

  return { vault, transactions, isPending, error, refetch: fetchVault };
}
