import React, { useState } from 'react';
import { View, StyleSheet } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { CustomInput } from '@/shared/ui';

interface LoginInputSectionProps {
  loginId: string;
  password: string;
  onLoginIdChange: (text: string) => void;
  onPasswordChange: (text: string) => void;
}

export function LoginInputSection({
  loginId,
  password,
  onLoginIdChange,
  onPasswordChange,
}: LoginInputSectionProps) {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <View style={styles.container}>
      <CustomInput
        label="이메일 아이디"
        value={loginId}
        onChangeText={onLoginIdChange}
        placeholder="example@donttaz.com"
        keyboardType="email-address"
        autoCapitalize="none"
      />

      <CustomInput
        label="비밀번호"
        value={password}
        onChangeText={onPasswordChange}
        placeholder="비밀번호를 입력해 주세요"
        secureTextEntry={!showPassword}
        autoCapitalize="none"
        rightIcon={
          <Feather
            name={showPassword ? 'eye' : 'eye-off'}
            size={20}
            color="#9CA3AF"
          />
        }
        onRightIconPress={() => setShowPassword((prev) => !prev)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
    gap: 16,
  },
});
