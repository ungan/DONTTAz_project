import React from 'react';
import { View, Text } from 'react-native';
import { NumericKeypad, PinIndicator } from '@/shared/ui';
import { Primary } from '@/shared/constants/theme';
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
          {isConfirming ? 'PIN을 한 번 더 입력해 주세요' : 'PIN 6자리를 입력해 주세요'}
        </Text>
        <Text style={styles.subtitle}>
          {isConfirming
            ? '처음 입력한 PIN과 동일하게 입력해 주세요'
            : '앱 재실행 시 사용할 간편 비밀번호예요'}
        </Text>
        <View style={styles.indicatorBlock}>
          <PinIndicator
            filledCount={isConfirming ? pinCodeConfirm.length : pinCode.length}
            filledColor={Primary}
          />
        </View>
        {pinError ? <Text style={styles.errorText}>{pinError}</Text> : null}
      </View>

      <NumericKeypad onKeyPress={onKeyPress} hiddenKeys={['00']} />
    </View>
  );
}
