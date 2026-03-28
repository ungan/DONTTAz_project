import React from 'react';
import { Text, View } from 'react-native';
import Svg, { Path } from 'react-native-svg';
import type { SpendingPlanCategoryPlan } from '@/entities/plan';
import { styles } from './spending-plan-pie-chart.styles';

const SIZE = 220;
const CX = SIZE / 2;
const CY = SIZE / 2;
const OUTER_R = 84;
const INNER_R = 54;

function formatWon(amount: number): string {
  return `${amount.toLocaleString('ko-KR')}원`;
}

function polarToCartesian(cx: number, cy: number, r: number, angleDeg: number) {
  const rad = ((angleDeg - 90) * Math.PI) / 180;
  return { x: cx + r * Math.cos(rad), y: cy + r * Math.sin(rad) };
}

function buildArcPath(startAngle: number, endAngle: number): string {
  const o1 = polarToCartesian(CX, CY, OUTER_R, startAngle);
  const o2 = polarToCartesian(CX, CY, OUTER_R, endAngle);
  const i1 = polarToCartesian(CX, CY, INNER_R, endAngle);
  const i2 = polarToCartesian(CX, CY, INNER_R, startAngle);
  const large = endAngle - startAngle > 180 ? 1 : 0;

  return [
    `M ${o1.x} ${o1.y}`,
    `A ${OUTER_R} ${OUTER_R} 0 ${large} 1 ${o2.x} ${o2.y}`,
    `L ${i1.x} ${i1.y}`,
    `A ${INNER_R} ${INNER_R} 0 ${large} 0 ${i2.x} ${i2.y}`,
    'Z',
  ].join(' ');
}

interface SpendingPlanPieChartProps {
  categoryPlans: SpendingPlanCategoryPlan[];
}

export function SpendingPlanPieChart({ categoryPlans }: SpendingPlanPieChartProps) {
  let currentAngle = 0;
  const slices = categoryPlans.map((item) => {
    const angle = (item.percentage / 100) * 360;
    const startAngle = currentAngle;
    const endAngle = currentAngle + angle;
    currentAngle = endAngle;

    return {
      ...item,
      path: buildArcPath(startAngle, endAngle),
    };
  });

  return (
    <View style={styles.card}>
      <Text style={styles.headerTitle}>카테고리별 예산</Text>

      <View style={styles.chartWrap}>
        <Svg width={SIZE} height={SIZE}>
          {slices.map((slice) => (
            <Path key={slice.categoryName} d={slice.path} fill={slice.color} />
          ))}
        </Svg>
      </View>

      <View style={styles.legend}>
        {categoryPlans.map((item) => (
          <View key={item.categoryName} style={styles.legendRow}>
            <View style={[styles.dot, { backgroundColor: item.color }]} />
            <View style={styles.legendInfo}>
              <Text style={styles.legendCategory}>{item.categoryName}</Text>
              <Text style={styles.legendMeta}>{item.percentage}% 배정</Text>
            </View>
            <Text style={styles.legendAmount}>{formatWon(item.allocatedAmount)}</Text>
          </View>
        ))}
      </View>
    </View>
  );
}
