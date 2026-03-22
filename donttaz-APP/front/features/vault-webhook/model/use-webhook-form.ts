import { useState } from 'react';
import type { WebhookPlatform } from '@/entities/vault';

export const WEBHOOK_MESSAGE_MAX_LENGTH = 200;

export function useWebhookForm() {
  const [platform, setPlatform] = useState<WebhookPlatform>('KAKAO_TALK');
  const [message, setMessage] = useState('');

  const isValid = message.trim().length > 0 && message.length <= WEBHOOK_MESSAGE_MAX_LENGTH;

  return {
    platform, setPlatform,
    message, setMessage,
    isValid,
  };
}
