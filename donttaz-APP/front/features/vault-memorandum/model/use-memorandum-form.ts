import { useState } from 'react';
import type { MemorandumRequest } from '@/entities/vault';

export function useMemorandumForm() {
  const [content1, setContent1] = useState('');
  const [content2, setContent2] = useState(
    '이 메시지를 확인한 지인들은 부디 나를 불쌍히 여기지 말고, 채팅창에 온갖 조롱과 멸시의 이모티콘을 쏟아부어 주길 바란다.',
  );
  const [content3, setContent3] = useState(
    '오늘부터 금고 해제 쿨다운이 끝날 때까지 나의 모든 저작 운동은 물과 공기로만 이루어질 예정이다.',
  );

  const isValid = content1.trim().length >= 2;

  const getRequestBody = (): MemorandumRequest => ({
    content1: content1.trim(),
    content2: content2.trim(),
    content3: content3.trim(),
  });

  return {
    content1, setContent1,
    content2, setContent2,
    content3, setContent3,
    isValid,
    getRequestBody,
  };
}
