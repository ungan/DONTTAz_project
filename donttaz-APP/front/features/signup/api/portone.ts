import * as WebBrowser from 'expo-web-browser';

export interface PortOneVerificationResult {
  identityVerificationId: string;
}

export interface PortOneVerificationQuery {
  identityVerificationId: string | null;
  errorCode: string | null;
  errorMessage: string | null;
}

export const PORTONE_CALLBACK_PATH = '/signup-start';
export const PORTONE_CALLBACK_URL = `front://${PORTONE_CALLBACK_PATH}`;

function getBackendOrigin() {
  const baseUrl = process.env.EXPO_PUBLIC_BASE_URL;

  if (!baseUrl) {
    throw new Error('EXPO_PUBLIC_BASE_URL이 설정되지 않았습니다.');
  }

  return baseUrl.replace(/\/api\/v1\/?$/, '');
}

function buildPortOneTestPageUrl(redirectUri: string) {
  const backendOrigin = getBackendOrigin();
  const query = new URLSearchParams({ redirectUri });

  return `${backendOrigin}/test-auth.html?${query.toString()}`;
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

export async function requestPortOneIdentityVerification(): Promise<PortOneVerificationResult> {
  const redirectUri = PORTONE_CALLBACK_URL;
  const authUrl = buildPortOneTestPageUrl(redirectUri);

  const result = await WebBrowser.openAuthSessionAsync(authUrl, redirectUri);

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

  throw new Error('본인인증 결과를 확인할 수 없습니다.');
}
