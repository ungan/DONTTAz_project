import { useState } from 'react';
import type { PlatformType } from '@/entities/vault';

export const WEBHOOK_MESSAGE_MAX_LENGTH = 200;

export function useWebhookForm() {
  const [platform, setPlatform] = useState<PlatformType>('MATTERMOST');
  const [message, setMessage] = useState('');

  const isValid = message.trim().length > 0 && message.length <= WEBHOOK_MESSAGE_MAX_LENGTH;

  return {
    platform, setPlatform,
    message, setMessage,
    isValid,
  };
}
