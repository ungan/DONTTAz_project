import type { AxiosInstance } from 'axios';
import { mockHandlers } from './handlers';

function urlToRegex(pattern: string): RegExp {
  const escaped = pattern
    .replace(/[.*+?^${}()|[\]\\]/g, '\\$&')  // 특수문자 이스케이프
    .replace(/:[a-zA-Z]+/g, '[^/]+');          // :param → 임의 경로 세그먼트
  return new RegExp(`${escaped}$`);
}

export function applyMockInterceptor(client: AxiosInstance) {
  if (process.env.EXPO_PUBLIC_IS_MOCK !== 'true') return;

  client.interceptors.request.use((config) => {
    const handler = mockHandlers.find(
      (h) =>
        h.method === config.method?.toUpperCase() &&
        urlToRegex(h.url).test(config.url ?? '')
    );

    if (handler) {
      config.adapter = () =>
        Promise.resolve({
          data: handler.response,
          status: 200,
          statusText: 'OK',
          headers: {},
          config,
        });
    }

    return config;
  });
}
