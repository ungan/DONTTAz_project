import React, { useState } from 'react';
import {
  View,
  TextInput,
  Text,
  StyleSheet,
  TextInputProps,
  TouchableOpacity,
} from 'react-native';
import { Primary } from '@/shared/constants/theme';

interface CustomInputProps extends TextInputProps {
  label?: string;
  errorMessage?: string;
  rightIcon?: React.ReactNode;
  onRightIconPress?: () => void;
}

export function CustomInput({
  label,
  errorMessage,
  rightIcon,
  onRightIconPress,
  style,
  ...props
}: CustomInputProps) {
  const [isFocused, setIsFocused] = useState(false);
  const isReadonly = props.editable === false;

  return (
    <View style={styles.wrapper}>
      {label && <Text style={styles.label}>{label}</Text>}

      <View
        style={[
          styles.inputContainer,
          !isReadonly && isFocused && styles.inputContainerFocused,
          !!errorMessage && styles.inputContainerError,
          isReadonly && styles.inputContainerReadonly,
        ]}
      >
        <TextInput
          style={[styles.input, isReadonly && styles.inputReadonly, style]}
          placeholderTextColor="#9CA3AF"
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          {...props}
        />
        {rightIcon && (
          <TouchableOpacity
            style={styles.rightIcon}
            activeOpacity={0.7}
            onPress={onRightIconPress}
            disabled={!onRightIconPress}
          >
            {rightIcon}
          </TouchableOpacity>
        )}
      </View>

      {!!errorMessage && (
        <Text style={styles.errorText}>{errorMessage}</Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    width: '100%',
    gap: 6,
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    color: '#374151',
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 52,
    borderWidth: 1,
    borderColor: '#D1D5DB',
    borderRadius: 8,
    paddingHorizontal: 16,
    backgroundColor: '#FFFFFF',
  },
  inputContainerFocused: {
    borderColor: Primary,
  },
  inputContainerError: {
    borderColor: '#EF4444',
  },
  inputContainerReadonly: {
    backgroundColor: '#F3F4F6',
    borderColor: '#E5E7EB',
  },
  inputReadonly: {
    color: '#9CA3AF',
  },
  input: {
    flex: 1,
    fontSize: 16,
    color: '#111827',
    padding: 0,
  },
  rightIcon: {
    marginLeft: 8,
  },
  errorText: {
    fontSize: 12,
    color: '#EF4444',
  },
});
