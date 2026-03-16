import React from 'react';
import { View } from 'react-native';
import { Image } from 'expo-image';
import { styles } from './mascot-slide.styles';

export function MascotSlide() {
  return (
    <View style={styles.container}>
      <Image
        source={require('@/shared/assets/mascots/donttaz-default.png')}
        style={styles.mascot}
        contentFit="contain"
      />
    </View>
  );
}
