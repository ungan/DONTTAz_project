import React from 'react';
import { View } from 'react-native';
import { MascotSlide } from './slides/mascot-slide';
import { styles } from './hero-banner.styles';

// TODO: 캐로슬 구현 예정 (DdaySlide, TimerSlide 추가)
const SLIDES = [<MascotSlide key="mascot" />];

export function HeroBanner() {
  return <View style={styles.container}>{SLIDES}</View>;
}
