import * as WebBrowser from 'expo-web-browser';
import * as Linking from 'expo-linking';
import { Platform } from 'react-native';
import { apiClient } from '@/shared/api';
import type { SignupApiResponse } from '../types/signup';

export interface PortOneVerificationResult {
  identityVerificationId: string;
}

export interface PortOneVerificationPendingResult {
  pending: true;
  sessionId: string;
}

export interface RequestPortOneIdentityVerificationOptions {
  callbackPath?: string;
}

export interface PortOneVerificationQuery {
  identityVerificationId: string | null;
  errorCode: string | null;
  errorMessage: string | null;
}

interface IdentityVerificationSessionStatusData {
  sessionId: string;
  status: 'PENDING' | 'SUCCEEDED' | 'FAILED';
  identityVerificationId: string | null;
  errorCode: string | null;
  errorMessage: string | null;
}

export const PORTONE_CALLBACK_PATH = '/signup-start';
const PORTONE_APP_SCHEME = 'front';
const PORTONE_POLL_INTERVAL_MS = 1500;
const PORTONE_POLL_TIMEOUT_MS = 300000;

function normalizeCallbackPath(callbackPath: string) {
  return callbackPath.startsWith('/') ? callbackPath : `/${callbackPath}`;
}

export function buildPortOneCallbackUrl(callbackPath: string = PORTONE_CALLBACK_PATH) {
  return Linking.createURL(normalizeCallbackPath(callbackPath), {
    scheme: PORTONE_APP_SCHEME,
  });
}

export const PORTONE_CALLBACK_URL = buildPortOneCallbackUrl(PORTONE_CALLBACK_PATH);

function getBackendOrigin() {
  const baseUrl = process.env.EXPO_PUBLIC_BASE_URL;

  if (!baseUrl) {
    throw new Error('EXPO_PUBLIC_BASE_URL이 설정되지 않았습니다.');
  }

  return baseUrl.replace(/\/api\/v1\/?$/, '');
}

function createPortOneSessionId() {
  if (typeof globalThis.crypto?.randomUUID === 'function') {
    return globalThis.crypto.randomUUID();
  }

  return `portone-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function buildPortOneTestPageUrl(redirectUri: string, sessionId: string) {
  const backendOrigin = getBackendOrigin();
  const query = new URLSearchParams({ redirectUri, sessionId });

  return `${backendOrigin}/test-auth.html?${query.toString()}`;
}

function getIdentityVerificationSessionPath(sessionId: string) {
  return `/auth/identity-verification-sessions/${encodeURIComponent(sessionId)}`;
}

function formatPortOneErrorMessage(
  errorCode: string | null,
  errorMessage: string | null,
  fallbackMessage: string
) {
  if (errorMessage && errorMessage.length > 0) {
    return errorCode && errorCode.length > 0 ? `[${errorCode}] ${errorMessage}` : errorMessage;
  }

  return fallbackMessage;
}

function delay(ms: number) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

function parseCallbackUrlInternal(url: string): PortOneVerificationQuery {
  const parsedUrl = new URL(url);
  const identityVerificationId = parsedUrl.searchParams.get('identityVerificationId');
  const errorCode = parsedUrl.searchParams.get('code');
  const errorMessage = parsedUrl.searchParams.get('message');

  if (identityVerificationId || errorCode || errorMessage) {
    return {
      identityVerificationId,
      errorCode,
      errorMessage,
    };
  }

  const nestedUrl = parsedUrl.searchParams.get('url');

  if (nestedUrl) {
    return parsePortOneCallbackUrl(decodeURIComponent(nestedUrl));
  }

  return {
    identityVerificationId: null,
    errorCode: null,
    errorMessage: null,
  };
}

export function parsePortOneCallbackUrl(url: string): PortOneVerificationQuery {
  try {
    return parseCallbackUrlInternal(url);
  } catch {
    return {
      identityVerificationId: null,
      errorCode: null,
      errorMessage: null,
    };
  }
}

export function matchesPortOneCallbackPath(url: string, callbackPath: string) {
  try {
    const parsedUrl = new URL(url);
    const normalizedCallbackPath = normalizeCallbackPath(callbackPath);
    const candidatePath = parsedUrl.pathname || (parsedUrl.host ? `/${parsedUrl.host}` : '');

    if (candidatePath === normalizedCallbackPath) {
      return true;
    }

    const nestedUrl = parsedUrl.searchParams.get('url');

    if (nestedUrl) {
      return matchesPortOneCallbackPath(decodeURIComponent(nestedUrl), normalizedCallbackPath);
    }

    return false;
  } catch {
    return false;
  }
}

export async function requestPortOneIdentityVerification(
  options: RequestPortOneIdentityVerificationOptions = {}
): Promise<PortOneVerificationResult | PortOneVerificationPendingResult> {
  const sessionId = createPortOneSessionId();
  const redirectUri = buildPortOneCallbackUrl(options.callbackPath ?? PORTONE_CALLBACK_PATH);
  const authUrl = buildPortOneTestPageUrl(redirectUri, sessionId);

  if (__DEV__) {
      console.log('[SignupDebug][PortOne] openAuthSession:start', {
      authUrl,
      redirectUri,
      sessionId,
      callbackPath: options.callbackPath ?? PORTONE_CALLBACK_PATH,
    });
  }

  // Android external identity apps can dismiss the auth session when the user
  // leaves Chrome Custom Tabs. Open the auth page in the system browser and
  // continue the flow through the deep link callback instead of waiting on the
  // browser session result.
  if (Platform.OS === 'android') {
    await Linking.openURL(authUrl);

    if (__DEV__) {
      console.log('[SignupDebug][PortOne] openAuthSession:androidExternalBrowser', {
        authUrl,
        redirectUri,
        sessionId,
      });
    }

    return { pending: true, sessionId };
  }

  const result = await WebBrowser.openAuthSessionAsync(authUrl, redirectUri);

  if (__DEV__) {
    console.log('[SignupDebug][PortOne] openAuthSession:result', result);
  }

  // In Expo Go on Android, switching to an external auth app can dismiss the
  // browser session before the deep link callback returns. Treat dismiss as a
  // pending state and let the screen-level deep link handler complete the flow.
  if (result.type === 'dismiss') {
    return { pending: true, sessionId };
  }

  if (result.type !== 'success') {
    throw new Error('본인인증이 취소되었거나 완료되지 않았습니다.');
  }

  const { identityVerificationId, errorCode, errorMessage } = parsePortOneCallbackUrl(
    result.url
  );

  if (identityVerificationId && identityVerificationId.length > 0) {
    return { identityVerificationId };
  }

  if (errorMessage && errorMessage.length > 0) {
    throw new Error(
      errorCode && errorCode.length > 0
        ? `[${errorCode}] ${errorMessage}`
        : errorMessage
    );
  }

  if (__DEV__) {
    console.log('[SignupDebug][PortOne] openAuthSession:missingIdentityVerificationId', {
      url: result.url,
    });
  }

  throw new Error('본인인증 결과를 확인할 수 없습니다.');
}

export async function pollPortOneIdentityVerificationSession(
  sessionId: string
): Promise<PortOneVerificationResult> {
  const startedAt = Date.now();

  while (Date.now() - startedAt <= PORTONE_POLL_TIMEOUT_MS) {
    const response = await apiClient.get<SignupApiResponse<IdentityVerificationSessionStatusData>>(
      getIdentityVerificationSessionPath(sessionId)
    );
    const session = response.data.data;

    if (__DEV__) {
      console.log('[SignupDebug][PortOne] pollSession:tick', session);
    }

    if (session.status === 'SUCCEEDED' && session.identityVerificationId) {
      return {
        identityVerificationId: session.identityVerificationId,
      };
    }

    if (session.status === 'FAILED') {
      throw new Error(
        formatPortOneErrorMessage(
          session.errorCode,
          session.errorMessage,
          '본인인증이 취소되었거나 완료되지 않았습니다.'
        )
      );
    }

    await delay(PORTONE_POLL_INTERVAL_MS);
  }

  throw new Error('본인인증 결과를 기다리는 중입니다. 인증 완료 후 앱으로 다시 돌아와 주세요.');
}
