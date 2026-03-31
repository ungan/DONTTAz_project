import { useEffect, useState } from 'react';
import { Text, View, TouchableOpacity } from 'react-native';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import {
  clearLocalAuthSession,
  useAuthRuntimeStore,
  useRefreshSession,
} from '@/features/auth';
import { getStoredDevicePin, getStoredRefreshToken } from '@/shared/api/auth-storage';
import { NumericKeypad, PinIndicator } from '@/shared/ui';
import { styles } from './pin-login-page.styles';

type KeyValue = '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '00' | 'X';

export function PinLoginPage() {
  const [pin, setPin] = useState('');
  const [storedPin, setStoredPin] = useState<string | null>(null);
  const [refreshToken, setRefreshToken] = useState<string | null>(null);
  const [localErrorMessage, setLocalErrorMessage] = useState<string | null>(null);
  const [isReady, setIsReady] = useState(false);
  const [isMovingToLogin, setIsMovingToLogin] = useState(false);
  const insets = useSafeAreaInsets();
  const setPinVerified = useAuthRuntimeStore(state => state.setPinVerified);
  const { refreshSession, isPending, error, resetError } = useRefreshSession();

  useEffect(() => {
    const loadPinLoginState = async () => {
      const [storedRefreshToken, localPin] = await Promise.all([
        getStoredRefreshToken(),
        getStoredDevicePin(),
      ]);

      if (!storedRefreshToken || !localPin) {
        router.replace('/login');
        return;
      }

      setRefreshToken(storedRefreshToken);
      setStoredPin(localPin);
      setIsReady(true);
    };

    void loadPinLoginState();
  }, []);

  const handleRefreshFailure = async () => {
    await clearLocalAuthSession();
    router.replace('/login');
  };

  const submitPin = async (value: string) => {
    if (!storedPin || !refreshToken) return;

    if (value !== storedPin) {
      setPin('');
      setLocalErrorMessage('PIN이 일치하지 않습니다. 다시 입력해 주세요.');
      return;
    }

    setLocalErrorMessage(null);
    resetError();

    const isSuccess = await refreshSession(refreshToken);

    if (!isSuccess) {
      await handleRefreshFailure();
      return;
    }

    setPinVerified(true);
  };

  const handleKeyPress = (key: KeyValue) => {
    if (isPending) return;

    setLocalErrorMessage(null);
    resetError();

    if (key === 'X') {
      setPin(prev => prev.slice(0, -1));
      return;
    }

    if (key === '00' || pin.length >= 6) return;

    const nextValue = `${pin}${key}`;
    setPin(nextValue);

    if (nextValue.length === 6) {
      void submitPin(nextValue);
    }
  };

  const handleGoToLogin = async () => {
    setIsMovingToLogin(true);
    try {
      await clearLocalAuthSession();
      router.replace('/login');
    } finally {
      setIsMovingToLogin(false);
    }
  };

  if (!isReady) return null;

  const errorMessage = localErrorMessage ?? error;

  return (
    <View style={[styles.container, { paddingTop: insets.top, paddingBottom: insets.bottom }]}>
      <View style={styles.pinSection}>
        <Text style={styles.title}>PIN 입력</Text>
        <Text style={styles.subtitle}>간편 PIN 6자리를 입력해 주세요</Text>
        <View style={styles.indicatorWrapper}>
          <PinIndicator filledCount={pin.length} />
        </View>
        {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}
        {isPending ? <Text style={styles.helperText}>세션을 복구하는 중입니다...</Text> : null}
      </View>

      <View style={styles.keypadSection}>
        <NumericKeypad onKeyPress={handleKeyPress} hiddenKeys={['00']} />
        <TouchableOpacity
          style={styles.switchAccountButton}
          onPress={handleGoToLogin}
          disabled={isMovingToLogin}
          activeOpacity={0.6}
        >
          <Text style={styles.switchAccountText}>다른 계정으로 로그인</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}
