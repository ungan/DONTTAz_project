import React from 'react';
import { View, Text, TextInput, TextInputProps } from 'react-native';
import { styles } from './inline-input.styles';

interface InlineInputProps extends TextInputProps {
  label: string;
}

export function InlineInput({ label, ...props }: InlineInputProps) {
  return (
    <View style={styles.container}>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        style={styles.input}
        placeholderTextColor="#9CA3AF"
        {...props}
      />
    </View>
  );
}
