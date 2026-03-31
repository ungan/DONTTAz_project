import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { styles } from './numeric-keypad.styles';

type KeyValue = '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '0' | '00' | 'X';

interface NumericKeypadProps {
  onKeyPress: (key: KeyValue) => void;
  hiddenKeys?: KeyValue[];
  labelColor?: string;
}

const ROWS: KeyValue[][] = [
  ['1', '2', '3'],
  ['4', '5', '6'],
  ['7', '8', '9'],
  ['00', '0', 'X'],
];

export function NumericKeypad({ onKeyPress, hiddenKeys = [], labelColor }: NumericKeypadProps) {
  const textColor = labelColor ?? '#111827';
  return (
    <View style={styles.container}>
      {ROWS.map((row, rowIndex) => (
        <View key={rowIndex} style={styles.row}>
          {row.map((key) => {
            if (hiddenKeys.includes(key)) {
              return <View key={key} style={styles.key} />;
            }
            return (
              <TouchableOpacity
                key={key}
                style={styles.key}
                onPress={() => onKeyPress(key)}
                activeOpacity={0.6}
              >
                {key === 'X' ? (
                  <Ionicons name="backspace-outline" size={28} color={textColor} />
                ) : (
                  <Text style={[styles.keyLabel, { color: textColor }]}>{key}</Text>
                )}
              </TouchableOpacity>
            );
          })}
        </View>
      ))}
    </View>
  );
}
