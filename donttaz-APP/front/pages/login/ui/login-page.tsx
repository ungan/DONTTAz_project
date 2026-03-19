import React, { useState } from 'react';
import { View, Text } from 'react-native';
import { router } from 'expo-router';
import { LoginFormSection, LoginActionSection, useLogin } from '@/features/auth';
import { styles } from './login-page.styles';

export function LoginPage() {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const { login, isPending, error } = useLogin();

  const handleLogin = () => {
    login(loginId, password);
  };

  const handleSignUp = () => {
    router.push('/signup-start' as never);
  };

  const handleFindId = () => {
    // TODO: 아이디 찾기 페이지 이동
  };

  const handleFindPassword = () => {
    // TODO: 비밀번호 찾기 페이지 이동
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>DONTTAz</Text>
        <Text style={styles.subtitle}>당신의 소비를 지켜보는 작은 감시자</Text>
      </View>

      <LoginFormSection
        loginId={loginId}
        password={password}
        onLoginIdChange={setLoginId}
        onPasswordChange={setPassword}
        onFindId={handleFindId}
        onFindPassword={handleFindPassword}
      />

      {error ? <Text style={styles.errorText}>{error}</Text> : null}

      <LoginActionSection
        onLogin={handleLogin}
        onSignUp={handleSignUp}
        isLoggingIn={isPending}
      />
    </View>
  );
}
