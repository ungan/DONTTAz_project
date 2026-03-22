import React, { useEffect, useRef } from 'react';
import { Animated } from 'react-native';
import { styles } from './sign-stamp-overlay.styles';

interface SignStampOverlayProps {
  visible: boolean;
  onAnimationEnd?: () => void;
}

export function SignStampOverlay({ visible, onAnimationEnd }: SignStampOverlayProps) {
  const overlayOpacity = useRef(new Animated.Value(0)).current;
  const stampScale = useRef(new Animated.Value(0.3)).current;
  const stampOpacity = useRef(new Animated.Value(0)).current;
  const stampRotate = useRef(new Animated.Value(-20)).current;

  useEffect(() => {
    if (!visible) return;

    Animated.sequence([
      Animated.timing(overlayOpacity, {
        toValue: 1,
        duration: 350,
        useNativeDriver: true,
      }),
      Animated.delay(80),
      Animated.parallel([
        Animated.spring(stampScale, {
          toValue: 1,
          friction: 3,
          tension: 130,
          useNativeDriver: true,
        }),
        Animated.timing(stampOpacity, {
          toValue: 1,
          duration: 120,
          useNativeDriver: true,
        }),
        Animated.spring(stampRotate, {
          toValue: -8,
          friction: 5,
          tension: 100,
          useNativeDriver: true,
        }),
      ]),
    ]).start(() => {
      setTimeout(() => onAnimationEnd?.(), 1000);
    });
  }, [visible]);

  if (!visible) return null;

  const rotateDeg = stampRotate.interpolate({
    inputRange: [-20, -8],
    outputRange: ['-20deg', '-8deg'],
  });

  return (
    <Animated.View style={[styles.overlay, { opacity: overlayOpacity }]} pointerEvents="none">
      <Animated.View
        style={[
          styles.stamp,
          {
            opacity: stampOpacity,
            transform: [{ scale: stampScale }, { rotate: rotateDeg }],
          },
        ]}
      >
        <Animated.View style={styles.stampInner}>
          <Animated.Text style={styles.stampText}>인증</Animated.Text>
        </Animated.View>
      </Animated.View>
    </Animated.View>
  );
}
