import React from 'react';
import { View } from 'react-native';
import { styles } from './pin-indicator.styles';

const PIN_LENGTH = 6;

interface PinIndicatorProps {
  filledCount: number;
  filledColor?: string;
  emptyBorderColor?: string;
}

export function PinIndicator({ filledCount, filledColor, emptyBorderColor }: PinIndicatorProps) {
  return (
    <View style={styles.container}>
      {Array.from({ length: PIN_LENGTH }).map((_, index) => (
        <View
          key={index}
          style={[
            styles.dot,
            emptyBorderColor ? { borderColor: emptyBorderColor } : undefined,
            index < filledCount && [
              styles.dotFilled,
              filledColor ? { backgroundColor: filledColor, borderColor: filledColor } : undefined,
            ],
          ]}
        />
      ))}
    </View>
  );
}
