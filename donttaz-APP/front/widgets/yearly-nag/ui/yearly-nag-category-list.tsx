import React, { useEffect } from 'react';
import { View, Text } from 'react-native';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withTiming,
  withDelay,
  Easing,
} from 'react-native-reanimated';
import { CategorySummary } from '@/entities/report';
import { styles } from './yearly-nag-category-list.styles';

const STAGGER_MS = 80;

interface AnimatedItemProps {
  cat: CategorySummary;
  index: number;
}

function AnimatedItem({ cat, index }: AnimatedItemProps) {
  const opacity = useSharedValue(0);
  const translateY = useSharedValue(12);

  useEffect(() => {
    const delay = index * STAGGER_MS;
    opacity.value = withDelay(delay, withTiming(1, { duration: 300, easing: Easing.out(Easing.ease) }));
    translateY.value = withDelay(delay, withTiming(0, { duration: 300, easing: Easing.out(Easing.ease) }));
  }, []);

  const animatedStyle = useAnimatedStyle(() => ({
    opacity: opacity.value,
    transform: [{ translateY: translateY.value }],
  }));

  return (
    <Animated.View style={[styles.item, animatedStyle]}>
      <View style={[styles.dot, { backgroundColor: cat.color }]} />
      <Text style={styles.category}>{cat.category}</Text>
      <Text style={styles.amount}>{cat.totalAmount.toLocaleString()}원</Text>
    </Animated.View>
  );
}

interface Props {
  categories: CategorySummary[];
}

export function YearlyNagCategoryList({ categories }: Props) {
  return (
    <View style={styles.container}>
      {categories.map((cat, index) => (
        <AnimatedItem key={cat.category} cat={cat} index={index} />
      ))}
    </View>
  );
}
