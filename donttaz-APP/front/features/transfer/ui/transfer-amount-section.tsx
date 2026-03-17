import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { styles } from './transfer-amount-section.styles';

const QUICK_AMOUNTS = [
  { label: '+1만', value: 10_000 },
  { label: '+5만', value: 50_000 },
  { label: '+10만', value: 100_000 },
];

interface TransferAmountSectionProps {
  onQuickAdd: (value: number) => void;
}

export function TransferAmountSection({ onQuickAdd }: TransferAmountSectionProps) {
  return (
    <View style={styles.container}>
      {QUICK_AMOUNTS.map(({ label, value }) => (
        <TouchableOpacity
          key={label}
          style={styles.quickButton}
          onPress={() => onQuickAdd(value)}
          activeOpacity={0.7}
        >
          <Text style={styles.quickButtonLabel}>{label}</Text>
        </TouchableOpacity>
      ))}
    </View>
  );
}
