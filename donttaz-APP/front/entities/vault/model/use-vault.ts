import { useState, useEffect, useCallback } from 'react';
import { getVaultApi } from '../api/get-vault';
import { getAllWishlistsApi } from '../api/get-all-wishlists';
import { getVaultTransactionsApi } from '../api/get-vault-transactions';
import type { Vault, VaultTransaction } from '../types/vault';

export function useVault() {
  const [defaultVault, setDefaultVault] = useState<Vault | null>(null);
  const [wishlistVault, setWishlistVault] = useState<Vault | null>(null);
  const [wishlistHistory, setWishlistHistory] = useState<Vault[]>([]);
  const [transactions, setTransactions] = useState<VaultTransaction[]>([]);
  const [wishlistTransactions, setWishlistTransactions] = useState<VaultTransaction[]>([]);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchVault = useCallback(async () => {
    setIsPending(true);
    setError(null);
    try {
      const [res, wishlistRes] = await Promise.all([getVaultApi(), getAllWishlistsApi()]);
      const dv = res.vaults.find((v) => v.vaultKind === 'DEFAULT') ?? null;
      const wv = wishlistRes.vaults.find((v) => v.status === 'LOCKED') ?? null;
      const wvHistory = wishlistRes.vaults.filter((v) => v.status !== 'LOCKED');
      setDefaultVault(dv);
      setWishlistVault(wv);
      setWishlistHistory(wvHistory);
      const [dvTx, wvTx] = await Promise.all([
        dv ? getVaultTransactionsApi('DEFAULT', dv.vaultId) : Promise.resolve({ data: [] }),
        wv ? getVaultTransactionsApi('WISHLIST', wv.vaultId) : Promise.resolve({ data: [] }),
      ]);
      setTransactions(dvTx.data);
      setWishlistTransactions(wvTx.data);
    } catch {
      setError('가상금고 정보를 불러오지 못했습니다.');
    } finally {
      setIsPending(false);
    }
  }, []);

  useEffect(() => {
    fetchVault();
  }, [fetchVault]);

  return { defaultVault, wishlistVault, wishlistHistory, transactions, wishlistTransactions, isPending, error, refetch: fetchVault };
}
