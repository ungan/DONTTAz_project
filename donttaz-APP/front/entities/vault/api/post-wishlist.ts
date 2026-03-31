import { apiClient } from '@/shared/api';
import type { WishlistRequest } from '../types/vault';

export async function postWishlistApi(vaultId: number, body: WishlistRequest): Promise<void> {
  await apiClient.post(`/vaults/${vaultId}/wishlist`, body);
}
