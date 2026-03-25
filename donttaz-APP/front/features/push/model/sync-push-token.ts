import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';

import { getOrCreatePushDeviceId, getStoredAccessToken } from '@/shared/api/auth-storage';
import { registerFcmTokenApi } from '../api/register-fcm-token';

function previewToken(token: string) {
  if (token.length <= 12) {
    return `${token.slice(0, 4)}***`;
  }

  return `${token.slice(0, 6)}***${token.slice(-6)}`;
}

export async function syncPushTokenWithServer() {
  try {
    const accessToken = await getStoredAccessToken();
    if (!accessToken) {
      return;
    }

    if (!Device.isDevice) {
      if (__DEV__) {
        console.log('[PushDebug] skip: physical device required');
      }
      return;
    }

    if (Platform.OS !== 'android' && Platform.OS !== 'ios') {
      if (__DEV__) {
        console.log('[PushDebug] skip: unsupported platform', Platform.OS);
      }
      return;
    }

    if (Platform.OS === 'android') {
      await Notifications.setNotificationChannelAsync('default', {
        name: 'default',
        importance: Notifications.AndroidImportance.MAX,
      });
    }

    const currentPermissions = await Notifications.getPermissionsAsync();
    let permissionStatus = currentPermissions.status;

    if (permissionStatus !== 'granted') {
      const requestedPermissions = await Notifications.requestPermissionsAsync();
      permissionStatus = requestedPermissions.status;
    }

    if (permissionStatus !== 'granted') {
      if (__DEV__) {
        console.log('[PushDebug] skip: notification permission denied');
      }
      return;
    }

    const devicePushToken = await Notifications.getDevicePushTokenAsync();
    const token = typeof devicePushToken.data === 'string' ? devicePushToken.data : String(devicePushToken.data ?? '');

    if (!token) {
      if (__DEV__) {
        console.log('[PushDebug] skip: empty device push token');
      }
      return;
    }

    const deviceId = await getOrCreatePushDeviceId();
    const deviceType = Platform.OS === 'ios' ? 'IOS' : 'ANDROID';

    await registerFcmTokenApi({
      fcmToken: token,
      deviceType,
      deviceId,
    });

    if (__DEV__) {
      console.log('[PushDebug] registered device push token', {
        deviceId,
        deviceType,
        tokenPreview: previewToken(token),
      });
    }
  } catch (error) {
    if (__DEV__) {
      console.log('[PushDebug] registration failed', {
        message: error instanceof Error ? error.message : String(error),
      });
    }
  }
}
