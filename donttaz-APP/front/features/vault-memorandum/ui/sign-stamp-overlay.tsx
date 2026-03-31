import React, { useEffect, useRef } from 'react';
import { Animated } from 'react-native';
import { styles } from './sign-stamp-overlay.styles';

interface SignStampOverlayProps {
  visible: boolean;
  onAnimationEnd?: () => void;
}

export function SignStampOverlay({ visible, onAnimationEnd }: SignStampOverlayProps) {
  const overlayOpacity = useRef(new Animated.Value(0)).current;
  const stampScale = useRef(new Animated.Value(1.4)).current;
  const stampOpacity = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (!visible) return;

    Animated.sequence([
      Animated.timing(overlayOpacity, {
        toValue: 1,
        duration: 150,
        useNativeDriver: true,
      }),
      Animated.parallel([
        Animated.timing(stampScale, {
          toValue: 1,
          duration: 120,
          useNativeDriver: true,
        }),
        Animated.timing(stampOpacity, {
          toValue: 1,
          duration: 80,
          useNativeDriver: true,
        }),
      ]),
    ]).start(() => {
      setTimeout(() => onAnimationEnd?.(), 900);
    });
  }, [visible]);

  if (!visible) return null;

  return (
    <Animated.View style={[styles.overlay, { opacity: overlayOpacity }]} pointerEvents="none">
      <Animated.View
        style={[
          styles.stamp,
          {
            opacity: stampOpacity,
            transform: [{ scale: stampScale }, { rotate: '-15deg' }],
          },
        ]}
      >
        <Animated.View style={styles.stampInner}>
          <Animated.Text style={styles.stampText}>APPROVED</Animated.Text>
        </Animated.View>
      </Animated.View>
    </Animated.View>
  );
}
