import React, { useRef, useCallback } from 'react';
import { Animated, PanResponder, View } from 'react-native';
import { MascotSlide } from './slides/mascot-slide';
import { DdaySlide } from './slides/dday-slide';
import { styles } from './hero-banner.styles';

export function HeroBanner() {
  const flipAnim = useRef(new Animated.Value(0)).current;
  const isFlippedRef = useRef(false);
  const isAnimatingRef = useRef(false);

  const flip = useCallback(() => {
    if (isAnimatingRef.current) return;
    isAnimatingRef.current = true;

    const toValue = isFlippedRef.current ? 0 : 1;
    Animated.timing(flipAnim, {
      toValue,
      duration: 400,
      useNativeDriver: true,
    }).start(() => {
      isFlippedRef.current = !isFlippedRef.current;
      isAnimatingRef.current = false;
    });
  }, [flipAnim]);

  const flipRef = useRef(flip);
  flipRef.current = flip;

  const panResponder = useRef(
    PanResponder.create({
      onMoveShouldSetPanResponder: (_, gs) =>
        Math.abs(gs.dx) > Math.abs(gs.dy) && Math.abs(gs.dx) > 10,
      onMoveShouldSetPanResponderCapture: (_, gs) =>
        Math.abs(gs.dx) > Math.abs(gs.dy) && Math.abs(gs.dx) > 10,
      onPanResponderRelease: (_, gs) => {
        if (Math.abs(gs.dx) > 30) flipRef.current();
      },
    }),
  ).current;

  const frontRotate = flipAnim.interpolate({
    inputRange: [0, 1],
    outputRange: ['0deg', '180deg'],
  });

  const backRotate = flipAnim.interpolate({
    inputRange: [0, 1],
    outputRange: ['180deg', '360deg'],
  });

  return (
    <View style={styles.container} {...panResponder.panHandlers}>
      <Animated.View
        style={[styles.card, { transform: [{ perspective: 1000 }, { rotateY: frontRotate }] }]}
      >
        <MascotSlide />
      </Animated.View>
      <Animated.View
        style={[styles.card, { transform: [{ perspective: 1000 }, { rotateY: backRotate }] }]}
      >
        <DdaySlide />
      </Animated.View>
    </View>
  );
}
