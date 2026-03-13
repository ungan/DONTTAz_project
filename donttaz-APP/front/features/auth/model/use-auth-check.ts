import { useEffect, useState } from 'react';

// TODO: API 연결 시 true로 변경 후 토큰 검증 로직 구현
const checked = false;

export function useAuthCheck(): 'loading' | 'authenticated' | 'unauthenticated' {
  const [status, setStatus] = useState<'loading' | 'authenticated' | 'unauthenticated'>('loading');

  useEffect(() => {
    const check = async () => {
      const [authResult] = await Promise.all([
        // TODO: checked=true 시 토큰 검증 로직으로 교체
        Promise.resolve(checked ? 'authenticated' : 'unauthenticated'),
        new Promise(resolve => setTimeout(resolve, 1500)),
      ]);
      setStatus(authResult as 'authenticated' | 'unauthenticated');
    };

    check();
  }, []);

  return status;
}
