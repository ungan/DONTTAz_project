import React, { useState } from 'react';
import { View, TextInput, StyleSheet } from 'react-native';
import { Feather } from '@expo/vector-icons';
import MaterialIcons from '@expo/vector-icons/MaterialIcons';

interface LoginInputSectionProps {
  email: string;
  password: string;
  onEmailChange: (text: string) => void;
  onPasswordChange: (text: string) => void;
}

export function LoginInputSection({
  email,
  password,
  onEmailChange,
  onPasswordChange,
}: LoginInputSectionProps) {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <View style={styles.container}>
      {/* ID */}
      <View style={styles.inputRow}>
        <Feather name="mail" size={24} color="#9CA3AF" />
        <TextInput
          style={styles.input}
          placeholder="ID"
          placeholderTextColor="#9CA3AF"
          value={email}
          onChangeText={onEmailChange}
          keyboardType="email-address"
          autoCapitalize="none"
        />
      </View>

      {/* 비밀번호 */}
      <View style={styles.inputRow}>
        <MaterialIcons name="lock-outline" size={26} color="#9CA3AF" />
        <TextInput
          style={styles.input}
          placeholder="영문, 숫자 포함 6~12자"
          placeholderTextColor="#9CA3AF"
          value={password}
          onChangeText={onPasswordChange}
          secureTextEntry={!showPassword}
          autoCapitalize="none"
        />
        <Feather
          name={showPassword ? 'eye' : 'eye-off'}
          size={20}
          color="#9CA3AF"
          onPress={() => setShowPassword((prev) => !prev)}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
  },
  inputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderBottomWidth: 2,
    borderBottomColor: '#DDD',
    gap: 12,
  },
  input: {
    flex: 1,
    fontSize: 16,
    color: '#111827',
    padding: 0,
  },
});
