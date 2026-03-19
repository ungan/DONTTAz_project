import React from 'react';
import { View, Text } from 'react-native';
import { CustomInput } from '@/shared/ui';
import { styles } from './signup-account-form.styles';

interface SignupAccountFormProps {
  loginId: string;
  password: string;
  passwordConfirm: string;
  loginIdError: string | null;
  passwordError: string | null;
  passwordConfirmError: string | null;
  onLoginIdChange: (value: string) => void;
  onPasswordChange: (value: string) => void;
  onPasswordConfirmChange: (value: string) => void;
}

export function SignupAccountForm({
  loginId,
  password,
  passwordConfirm,
  loginIdError,
  passwordError,
  passwordConfirmError,
  onLoginIdChange,
  onPasswordChange,
  onPasswordConfirmChange,
}: SignupAccountFormProps) {
  return (
    <View style={styles.container}>
      <CustomInput
        label="이메일 아이디"
        value={loginId}
        onChangeText={onLoginIdChange}
        placeholder="example@donttaz.com"
        keyboardType="email-address"
        autoCapitalize="none"
        errorMessage={loginIdError ?? undefined}
      />

      <CustomInput
        label="비밀번호"
        value={password}
        onChangeText={onPasswordChange}
        placeholder="영문, 숫자, 특수문자 포함 8~16자"
        secureTextEntry
        autoCapitalize="none"
        errorMessage={passwordError ?? undefined}
      />

      <Text style={styles.helperText}>
        백엔드 규칙에 맞춰 영문, 숫자, 특수문자를 모두 포함해야 합니다.
      </Text>

      <CustomInput
        label="비밀번호 확인"
        value={passwordConfirm}
        onChangeText={onPasswordConfirmChange}
        placeholder="비밀번호를 다시 입력해 주세요."
        secureTextEntry
        autoCapitalize="none"
        errorMessage={passwordConfirmError ?? undefined}
      />
    </View>
  );
}
