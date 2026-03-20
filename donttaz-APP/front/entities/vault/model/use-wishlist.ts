import { useState, useEffect, useCallback } from 'react';
import { getWishlistApi } from '../api/get-wishlist';
import { postWishlistApi } from '../api/post-wishlist';
import { patchWishlistApi } from '../api/patch-wishlist';
import { useWishlistStore } from './wishlist-store';
import type { WishlistRequest } from '../types/vault';

export function useWishlist(vaultId: number | null) {
  const { wishlists, setWishlists } = useWishlistStore();
  const [isPending, setIsPending] = useState(false);

  const fetchWishlists = useCallback(async () => {
    if (!vaultId) return;
    setIsPending(true);
    try {
      const res = await getWishlistApi(vaultId);
      setWishlists(res.data);
    } finally {
      setIsPending(false);
    }
  }, [vaultId]);

  useEffect(() => {
    fetchWishlists();
  }, [fetchWishlists]);

  const post = async (body: WishlistRequest) => {
    if (!vaultId) return;
    await postWishlistApi(vaultId, body);
    await fetchWishlists();
  };

  const patch = async (wishId: number, body: WishlistRequest) => {
    if (!vaultId) return;
    await patchWishlistApi(vaultId, wishId, body);
    await fetchWishlists();
  };

  return { wishlists, isPending, post, patch };
}
