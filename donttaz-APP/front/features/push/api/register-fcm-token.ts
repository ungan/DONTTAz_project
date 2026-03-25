import { apiClient } from '@/shared/api';

export type PushDeviceType = 'ANDROID' | 'IOS';

export interface RegisterFcmTokenRequest {
  fcmToken: string;
  deviceType: PushDeviceType;
  deviceId: string;
}

export async function registerFcmTokenApi(body: RegisterFcmTokenRequest) {
  return apiClient.post('/users/fcm-token', body);
}
