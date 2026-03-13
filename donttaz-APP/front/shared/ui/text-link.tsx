import React from 'react';
import { Text, StyleSheet, TextProps, TouchableOpacity } from 'react-native';
import { Primary } from '@/shared/constants/theme';

interface TextLinkProps extends TextProps {
  label: string;
  onPress: () => void;
  underline?: boolean;
  color?: string;
  size?: number;
}

export function TextLink({
  label,
  onPress,
  underline = false,
  color = Primary,
  size = 14,
  style,
  ...props
}: TextLinkProps) {
  return (
    <TouchableOpacity onPress={onPress} activeOpacity={0.7}>
      <Text
        style={[
          styles.text,
          {
            color,
            fontSize: size,
            textDecorationLine: underline ? 'underline' : 'none',
          },
          style,
        ]}
        {...props}
      >
        {label}
      </Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  text: {
    fontWeight: '500',
  },
});
