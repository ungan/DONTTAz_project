import { apiClient } from '@/shared/api';
import type { WishlistRequest } from '../types/vault';

export async function postWishlistApi(vaultId: number, body: WishlistRequest): Promise<void> {
  await apiClient.post(`/api/v1/vaults/${vaultId}/wishlist`, body);
}
