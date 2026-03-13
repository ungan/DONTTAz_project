import React, { useState } from 'react';
import { View, Text } from 'react-native';
import { LoginFormSection, LoginActionSection } from '@/features/auth';
import { styles } from './login-page.styles';

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = () => {
    // TODO: 로그인 로직
  };

  const handleSignUp = () => {
    // TODO: 회원가입 페이지 이동
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
        <Text style={styles.subtitle}>당신의 월급을 지키는 작은 감시자</Text>
      </View>

      <LoginFormSection
        email={email}
        password={password}
        onEmailChange={setEmail}
        onPasswordChange={setPassword}
        onFindId={handleFindId}
        onFindPassword={handleFindPassword}
      />

      <LoginActionSection
        onLogin={handleLogin}
        onSignUp={handleSignUp}
      />
    </View>
  );
}
