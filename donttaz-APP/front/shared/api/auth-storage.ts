import * as SecureStore from 'expo-secure-store';

const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const DEVICE_PIN_KEY = 'devicePin';
const PUSH_DEVICE_ID_KEY = 'pushDeviceId';

function generatePushDeviceId() {
  return `push-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`;
}

export async function storeAuthTokens(accessToken: string, refreshToken: string) {
  await SecureStore.setItemAsync(ACCESS_TOKEN_KEY, accessToken);
  await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, refreshToken);
}

export async function getStoredAccessToken() {
  return SecureStore.getItemAsync(ACCESS_TOKEN_KEY);
}

export async function getStoredRefreshToken() {
  return SecureStore.getItemAsync(REFRESH_TOKEN_KEY);
}

export async function clearStoredAuthTokens() {
  await SecureStore.deleteItemAsync(ACCESS_TOKEN_KEY);
  await SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY);
}

export async function storeDevicePin(pin: string) {
  await SecureStore.setItemAsync(DEVICE_PIN_KEY, pin);
}

export async function getStoredDevicePin() {
  return SecureStore.getItemAsync(DEVICE_PIN_KEY);
}

export async function clearStoredDevicePin() {
  await SecureStore.deleteItemAsync(DEVICE_PIN_KEY);
}

export async function getOrCreatePushDeviceId() {
  const storedDeviceId = await SecureStore.getItemAsync(PUSH_DEVICE_ID_KEY);
  if (storedDeviceId) {
    return storedDeviceId;
  }

  const generatedDeviceId = generatePushDeviceId();
  await SecureStore.setItemAsync(PUSH_DEVICE_ID_KEY, generatedDeviceId);
  return generatedDeviceId;
}
