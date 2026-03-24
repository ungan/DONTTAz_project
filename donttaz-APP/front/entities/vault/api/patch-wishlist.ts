import { apiClient } from '@/shared/api';
import type { WishlistRequest } from '../types/vault';

export async function patchWishlistApi(
  vaultId: number,
  wishId: number,
  body: WishlistRequest
): Promise<void> {
  await apiClient.patch(`/vaults/${vaultId}/wishlist/${wishId}`, body);
}
