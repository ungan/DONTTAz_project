import { apiClient } from '@/shared/api';
import type { CreateWishlistVaultRequest, CreateWishlistVaultResponse, CreateWishlistVaultData } from '../types/vault';

export async function createWishlistVaultApi(
  body: CreateWishlistVaultRequest
): Promise<CreateWishlistVaultData> {
  const response = await apiClient.post<CreateWishlistVaultResponse>('/vaults/wishlist', body);
  return response.data.data;
}
