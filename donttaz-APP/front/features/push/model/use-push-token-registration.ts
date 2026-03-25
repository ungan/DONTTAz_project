import { useEffect } from 'react';

import { syncPushTokenWithServer } from './sync-push-token';

export function usePushTokenRegistration() {
  useEffect(() => {
    void syncPushTokenWithServer();
  }, []);
}
