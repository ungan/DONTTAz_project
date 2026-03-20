import { apiClient } from '@/shared/api';
import type { GetWishlistResponse } from '../types/vault';

export async function getWishlistApi(vaultId: number): Promise<GetWishlistResponse> {
  const response = await apiClient.get<GetWishlistResponse>(
    `/api/v1/vaults/${vaultId}/wishlist`
  );
  return response.data;
}
