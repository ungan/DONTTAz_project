import { useState, useEffect, useCallback } from 'react';
import { getVaultApi } from '../api/get-vault';
import { getVaultTransactionsApi } from '../api/get-vault-transactions';
import type { Vault, VaultTransaction } from '../types/vault';

export function useVault() {
  const [defaultVault, setDefaultVault] = useState<Vault | null>(null);
  const [wishlistVault, setWishlistVault] = useState<Vault | null>(null);
  const [transactions, setTransactions] = useState<VaultTransaction[]>([]);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchVault = useCallback(async () => {
    setIsPending(true);
    setError(null);
    try {
      const res = await getVaultApi();
      console.log('[useVault] response:', JSON.stringify(res));
      const dv = res.vaults.find((v) => v.vaultKind === 'DEFAULT') ?? null;
      const wv = res.vaults.find((v) => v.vaultKind === 'WISHLIST') ?? null;
      setDefaultVault(dv);
      setWishlistVault(wv);
      if (dv) {
        const txRes = await getVaultTransactionsApi(dv.vaultId);
        setTransactions(txRes.data);
      }
    } catch (e) {
      console.error('[useVault] error:', e);
      setError('가상금고 정보를 불러오지 못했습니다.');
    } finally {
      setIsPending(false);
    }
  }, []);

  useEffect(() => {
    fetchVault();
  }, [fetchVault]);

  return { defaultVault, wishlistVault, transactions, isPending, error, refetch: fetchVault };
}
