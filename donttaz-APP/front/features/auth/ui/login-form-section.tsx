import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { TextLink } from '@/shared/ui';
import { LoginInputSection } from './login-input-section';

interface LoginFormSectionProps {
  email: string;
  password: string;
  onEmailChange: (text: string) => void;
  onPasswordChange: (text: string) => void;
  onFindId: () => void;
  onFindPassword: () => void;
}

export function LoginFormSection({
  email,
  password,
  onEmailChange,
  onPasswordChange,
  onFindId,
  onFindPassword,
}: LoginFormSectionProps) {
  return (
    <View style={styles.container}>
      <LoginInputSection
        email={email}
        password={password}
        onEmailChange={onEmailChange}
        onPasswordChange={onPasswordChange}
      />

      {/* 아이디 찾기 | 비밀번호 찾기 */}
      <View style={styles.findRow}>
        <TextLink
          label="아이디 찾기"
          onPress={onFindId}
          color="#757575"
          size={16}
          style={styles.findLink}
          numberOfLines={1}
        />
        <Text style={styles.divider}>|</Text>
        <TextLink
          label="비밀번호 찾기"
          onPress={onFindPassword}
          color="#757575"
          size={16}
          style={styles.findLink}
          numberOfLines={1}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: 330,
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 16,
  },
  findRow: {
    flexDirection: 'row',
    alignSelf: 'flex-end',
    alignItems: 'center',
    gap: 8,
  },
  findLink: {
    fontFamily: 'Roboto',
    fontWeight: '400',
  },
  divider: {
    fontSize: 16,
    color: '#D1D5DB',
  },
});
