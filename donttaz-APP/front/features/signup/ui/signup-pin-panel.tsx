import React from 'react';
import { View, Text } from 'react-native';
import { NumericKeypad, PinIndicator } from '@/shared/ui';
import { styles } from './signup-pin-panel.styles';

interface SignupPinPanelProps {
  pinCode: string;
  pinCodeConfirm: string;
  pinError: string | null;
  onKeyPress: (key: '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '00' | 'X') => void;
}

export function SignupPinPanel({
  pinCode,
  pinCodeConfirm,
  pinError,
  onKeyPress,
}: SignupPinPanelProps) {
  const isConfirming = pinCode.length === 6;

  return (
    <View style={styles.container}>
      <View style={styles.section}>
        <Text style={styles.title}>
          {isConfirming ? '한 번 더 입력해 주세요' : '간편 PIN을 설정해 주세요'}
        </Text>
        <Text style={styles.subtitle}>
          {isConfirming
            ? '같은 숫자 6자리를 다시 입력하면 설정이 완료됩니다.'
            : '앱 재진입 시 사용할 숫자 6자리를 입력해 주세요.'}
        </Text>
      </View>

      <View style={styles.indicatorBlock}>
        <Text style={styles.indicatorLabel}>PIN 입력</Text>
        <PinIndicator filledCount={pinCode.length} />
      </View>

      <View style={styles.indicatorBlock}>
        <Text style={styles.indicatorLabel}>PIN 확인</Text>
        <PinIndicator filledCount={pinCodeConfirm.length} />
      </View>

      {pinError ? <Text style={styles.errorText}>{pinError}</Text> : null}

      <NumericKeypad onKeyPress={onKeyPress} hiddenKeys={['00']} />
    </View>
  );
}
