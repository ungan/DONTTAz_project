import { apiClient } from '@/shared/api';
import type { PenaltyWebhookResponse } from '../types/vault';

export async function getPenaltyWebhookApi(vaultId: number): Promise<PenaltyWebhookResponse | null> {
  try {
    const res = await apiClient.get(`/penalty-webhooks/${vaultId}`);
    return res.data?.data ?? res.data ?? null;
  } catch {
    return null;
  }
}
