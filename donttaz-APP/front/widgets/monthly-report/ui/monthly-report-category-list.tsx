import React, { useEffect } from 'react';
import { View, Text } from 'react-native';
import Svg, { Circle, Path } from 'react-native-svg';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withTiming,
  withDelay,
  Easing,
} from 'react-native-reanimated';
import { CategorySummary } from '@/entities/report';
import { styles } from './monthly-report-category-list.styles';

const MINI_SIZE = 44;
const MINI_CX = MINI_SIZE / 2;
const MINI_CY = MINI_SIZE / 2;
const MINI_R = 18;
const MINI_STROKE = 5;
const STAGGER_MS = 80;

function miniArcPath(percentage: number): string {
  const angle = (percentage / 100) * 360;
  const clampedAngle = Math.min(angle, 359.99);
  const rad = ((clampedAngle - 90) * Math.PI) / 180;
  const x = MINI_CX + MINI_R * Math.cos(rad);
  const y = MINI_CY + MINI_R * Math.sin(rad);
  const large = clampedAngle > 180 ? 1 : 0;
  return `M ${MINI_CX} ${MINI_CY - MINI_R} A ${MINI_R} ${MINI_R} 0 ${large} 1 ${x} ${y}`;
}

function MiniDonut({ percentage, color }: { percentage: number; color: string }) {
  return (
    <Svg width={MINI_SIZE} height={MINI_SIZE}>
      <Circle
        cx={MINI_CX}
        cy={MINI_CY}
        r={MINI_R}
        stroke="#F3F4F6"
        strokeWidth={MINI_STROKE}
        fill="none"
      />
      <Path
        d={miniArcPath(percentage)}
        stroke={color}
        strokeWidth={MINI_STROKE}
        strokeLinecap="round"
        fill="none"
      />
    </Svg>
  );
}

interface AnimatedItemProps {
  cat: CategorySummary;
  index: number;
  isLast: boolean;
}

function AnimatedItem({ cat, index, isLast }: AnimatedItemProps) {
  const opacity = useSharedValue(0);
  const translateY = useSharedValue(12);

  useEffect(() => {
    const delay = index * STAGGER_MS;
    opacity.value = withDelay(
      delay,
      withTiming(1, { duration: 300, easing: Easing.out(Easing.ease) })
    );
    translateY.value = withDelay(
      delay,
      withTiming(0, { duration: 300, easing: Easing.out(Easing.ease) })
    );
  }, []);

  const animatedStyle = useAnimatedStyle(() => ({
    opacity: opacity.value,
    transform: [{ translateY: translateY.value }],
  }));

  return (
    <Animated.View style={[styles.item, !isLast && styles.itemBorder, animatedStyle]}>
      <MiniDonut percentage={cat.percentage} color={cat.color} />
      <View style={styles.info}>
        <View style={styles.topRow}>
          <View style={styles.categoryRow}>
            <Text style={styles.category}>{cat.category}</Text>
            {cat.budgetAchievementRate !== null ? (
              <View style={[styles.badge, cat.budgetAchievementRate > 100 ? styles.badgeOver : styles.badgeOk]}>
                <Text style={[styles.badgeText, cat.budgetAchievementRate > 100 ? styles.badgeTextOver : styles.badgeTextOk]}>
                  {cat.budgetAchievementRate > 100 ? '초과' : '준수'}
                </Text>
              </View>
            ) : (
              <View style={[styles.badge, styles.badgeUnplanned]}>
                <Text style={[styles.badgeText, styles.badgeTextUnplanned]}>미계획</Text>
              </View>
            )}
          </View>
          <Text style={styles.amount}>{`${cat.totalAmount.toLocaleString()}\uC6D0`}</Text>
        </View>
        <Text style={styles.meta}>{`${cat.percentage}%`}</Text>
      </View>
    </Animated.View>
  );
}

interface Props {
  categories: CategorySummary[];
}

export function MonthlyReportCategoryList({ categories }: Props) {
  return (
    <View style={styles.container}>
      {categories.map((cat, index) => (
        <AnimatedItem
          key={cat.category}
          cat={cat}
          index={index}
          isLast={index === categories.length - 1}
        />
      ))}
    </View>
  );
}
