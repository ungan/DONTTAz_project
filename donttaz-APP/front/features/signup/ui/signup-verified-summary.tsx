import React from 'react';
import { View, Text } from 'react-native';
import { styles } from './signup-verified-summary.styles';

interface SignupVerifiedSummaryProps {
  name: string;
  phoneNumber: string;
}

function formatPhoneNumber(phoneNumber: string) {
  if (phoneNumber.length !== 11) {
    return phoneNumber;
  }

  return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3, 7)}-${phoneNumber.slice(7)}`;
}

export function SignupVerifiedSummary({
  name,
  phoneNumber,
}: SignupVerifiedSummaryProps) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>✓ 본인인증 완료</Text>
      <View style={styles.row}>
        <Text style={styles.label}>이름</Text>
        <Text style={styles.value}>{name}</Text>
      </View>
      <View style={styles.row}>
        <Text style={styles.label}>휴대폰 번호</Text>
        <Text style={styles.value}>{formatPhoneNumber(phoneNumber)}</Text>
      </View>
    </View>
  );
}
