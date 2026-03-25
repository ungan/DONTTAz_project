import { apiClient } from '@/shared/api';
import type { PenaltyWebhookRequest } from '../types/vault';

export async function postPenaltyWebhookApi(
  vaultId: number,
  body: PenaltyWebhookRequest
): Promise<void> {
  await apiClient.post(`/penalty-webhooks/${vaultId}`, body);
}

