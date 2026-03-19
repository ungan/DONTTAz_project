import React from 'react';
import { View } from 'react-native';
import { Button, TextLink } from '@/shared/ui';
import { styles } from './login-action-section.styles';

interface LoginActionSectionProps {
  onLogin: () => void;
  onSignUp: () => void;
  isLoggingIn?: boolean;
}

export function LoginActionSection({
  onLogin,
  onSignUp,
  isLoggingIn = false,
}: LoginActionSectionProps) {
  return (
    <View style={styles.container}>
      <Button
        label="로그인"
        onPress={onLogin}
        isLoading={isLoggingIn}
        style={styles.loginButton}
      />
      <TextLink
        label="이메일로 회원가입"
        onPress={onSignUp}
        color="#757575"
        size={16}
        underline
        style={styles.signUpLink}
        numberOfLines={1}
      />
    </View>
  );
}
