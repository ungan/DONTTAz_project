import React, { useEffect } from 'react';
import { View, Text } from 'react-native';
import Svg, { Path, G } from 'react-native-svg';
import Animated, {
  useSharedValue,
  useAnimatedProps,
  useAnimatedStyle,
  withTiming,
  Easing,
  SharedValue,
} from 'react-native-reanimated';
import { CategorySummary } from '@/entities/report';
import { styles } from './monthly-report-donut-chart.styles';

const AnimatedPath = Animated.createAnimatedComponent(Path);

const SIZE = 220;
const CX = SIZE / 2;
const CY = SIZE / 2;
const OUTER_R = 90;
const INNER_R = 60;
const LABEL_R = OUTER_R + 20;
const MIN_VISIBLE_LABEL_PERCENT = 5;

function polarToCartesian(cx: number, cy: number, r: number, angleDeg: number) {
  'worklet';
  const rad = ((angleDeg - 90) * Math.PI) / 180;
  return { x: cx + r * Math.cos(rad), y: cy + r * Math.sin(rad) };
}

function buildArcPath(startAngle: number, endAngle: number): string {
  'worklet';
  const clampedEnd = Math.min(endAngle, startAngle + 359.99);
  const o1 = polarToCartesian(CX, CY, OUTER_R, startAngle);
  const o2 = polarToCartesian(CX, CY, OUTER_R, clampedEnd);
  const i1 = polarToCartesian(CX, CY, INNER_R, clampedEnd);
  const i2 = polarToCartesian(CX, CY, INNER_R, startAngle);
  const large = clampedEnd - startAngle > 180 ? 1 : 0;
  return [
    `M ${o1.x} ${o1.y}`,
    `A ${OUTER_R} ${OUTER_R} 0 ${large} 1 ${o2.x} ${o2.y}`,
    `L ${i1.x} ${i1.y}`,
    `A ${INNER_R} ${INNER_R} 0 ${large} 0 ${i2.x} ${i2.y}`,
    'Z',
  ].join(' ');
}

interface SliceProps {
  startAngle: number;
  endAngle: number;
  color: string;
  progress: SharedValue<number>;
}

function AnimatedSlice({ startAngle, endAngle, color, progress }: SliceProps) {
  const animatedProps = useAnimatedProps(() => {
    const totalDrawn = 360 * progress.value;
    if (totalDrawn <= startAngle) return { d: '' };
    const currentEnd = Math.min(totalDrawn, endAngle);
    if (currentEnd - startAngle < 0.01) return { d: '' };
    return { d: buildArcPath(startAngle, currentEnd) };
  });

  return <AnimatedPath animatedProps={animatedProps} fill={color} />;
}

interface Props {
  categories: CategorySummary[];
}

export function MonthlyReportDonutChart({ categories }: Props) {
  const progress = useSharedValue(0);
  const centerOpacity = useSharedValue(0);

  useEffect(() => {
    progress.value = 0;
    centerOpacity.value = 0;
    progress.value = withTiming(1, {
      duration: 800,
      easing: Easing.out(Easing.cubic),
    });
    centerOpacity.value = withTiming(1, {
      duration: 400,
      easing: Easing.in(Easing.ease),
    });
  }, [categories]);

  const centerStyle = useAnimatedStyle(() => ({
    opacity: centerOpacity.value,
  }));

  let currentAngle = 0;
  const slices = categories.map((cat) => {
    const sliceAngle = (cat.percentage / 100) * 360;
    const midAngle = currentAngle + sliceAngle / 2;
    const startAngle = currentAngle;
    const endAngle = currentAngle + sliceAngle;
    const labelPos = polarToCartesian(CX, CY, LABEL_R, midAngle);
    currentAngle += sliceAngle;
    return { ...cat, startAngle, endAngle, labelPos };
  });

  return (
    <View style={styles.container}>
      <Svg width={SIZE} height={SIZE}>
        <G>
          {slices.map((slice) => (
            <AnimatedSlice
              key={slice.category}
              startAngle={slice.startAngle}
              endAngle={slice.endAngle}
              color={slice.color}
              progress={progress}
            />
          ))}
        </G>
      </Svg>
      {slices
        .filter((slice) => slice.percentage >= MIN_VISIBLE_LABEL_PERCENT)
        .map((slice) => (
          <Text
            key={slice.category}
            style={[
              styles.percentLabel,
              {
                left: slice.labelPos.x - 22,
                top: slice.labelPos.y - 8,
              },
            ]}
          >
            {slice.percentage}%
          </Text>
        ))}
    </View>
  );
}
