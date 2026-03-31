import React, { useRef, useState } from 'react';
import {
  Animated,
  NativeScrollEvent,
  NativeSyntheticEvent,
  ScrollView,
  View,
} from 'react-native';
import { MascotSlide } from './slides/mascot-slide';
import { DdaySlide } from './slides/dday-slide';
import { styles } from './hero-banner.styles';

const SLIDES = [MascotSlide, DdaySlide];

export function HeroBanner() {
  const [slideWidth, setSlideWidth] = useState(0);
  const [activeIndex, setActiveIndex] = useState(0);
  const indicatorAnims = useRef(SLIDES.map((_, i) => new Animated.Value(i === 0 ? 1 : 0))).current;

  const handleScroll = (e: NativeSyntheticEvent<NativeScrollEvent>) => {
    if (slideWidth === 0) return;
    const index = Math.round(e.nativeEvent.contentOffset.x / slideWidth);
    if (index !== activeIndex && index >= 0 && index < SLIDES.length) {
      indicatorAnims.forEach((anim, i) => {
        Animated.timing(anim, {
          toValue: i === index ? 1 : 0,
          duration: 200,
          useNativeDriver: false,
        }).start();
      });
      setActiveIndex(index);
    }
  };

  return (
    <View
      style={styles.container}
      onLayout={(e) => setSlideWidth(e.nativeEvent.layout.width)}
    >
      {slideWidth > 0 && (
        <ScrollView
          horizontal
          pagingEnabled
          showsHorizontalScrollIndicator={false}
          onScroll={handleScroll}
          scrollEventThrottle={16}
          decelerationRate="fast"
        >
          {SLIDES.map((Slide, i) => (
            <View key={i} style={[styles.slide, { width: slideWidth }]}>
              <Slide />
            </View>
          ))}
        </ScrollView>
      )}

      <View style={styles.indicators}>
        {SLIDES.map((_, i) => {
          const width = indicatorAnims[i].interpolate({
            inputRange: [0, 1],
            outputRange: [6, 20],
          });
          const backgroundColor = indicatorAnims[i].interpolate({
            inputRange: [0, 1],
            outputRange: ['#D1D5DB', '#5899FD'],
          });
          return <Animated.View key={i} style={[styles.indicator, { width, backgroundColor }]} />;
        })}
      </View>
    </View>
  );
}
