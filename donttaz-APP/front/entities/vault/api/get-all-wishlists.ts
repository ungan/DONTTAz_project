import { apiClient } from '@/shared/api';
import type { GetVaultsResponse } from '../types/vault';

export async function getAllWishlistsApi(): Promise<GetVaultsResponse> {
  const response = await apiClient.get<{ data: GetVaultsResponse }>('/vaults/wishlist');
  return response.data.data;
}
