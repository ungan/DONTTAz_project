import React, { useCallback } from 'react';
import { View, Text, TouchableOpacity, Dimensions } from 'react-native';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withTiming,
  withDelay,
} from 'react-native-reanimated';
import { useFocusEffect } from 'expo-router';
import Svg, {
  Path,
  Defs,
  LinearGradient,
  Stop,
  Circle,
  Text as SvgText,
} from 'react-native-svg';
import { FontAwesome5 } from '@expo/vector-icons';
import type { BurndownData } from '@/entities/plan';
import { styles, CHART_HEIGHT, CHART_LABEL_HEIGHT } from './burndown-chart.styles';

const SCREEN_WIDTH = Dimensions.get('window').width;
const OUTER_PADDING = 16;
const CARD_PADDING = 20;
const CHART_WIDTH = SCREEN_WIDTH - OUTER_PADDING * 2 - CARD_PADDING * 2;

interface BurndownChartProps {
  data: BurndownData;
  yearMonth: string;
  vsIdealPercent: number;
  daysUntilBankrupt: number | null;
  onToggleView: () => void;
}

function formatWon(amount: number): string {
  return amount.toLocaleString('ko-KR') + '원';
}

function getMonthLabel(yearMonth: string): string {
  const [year, month] = yearMonth.split('-');
  return `${year}년 ${parseInt(month)}월`;
}

function toLinePath(points: { x: number; y: number }[]): string {
  return points
    .map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x.toFixed(1)} ${p.y.toFixed(1)}`)
    .join(' ');
}

export function BurndownChart({ data, yearMonth, vsIdealPercent, daysUntilBankrupt, onToggleView }: BurndownChartProps) {
  const bannerOpacity = useSharedValue(0);
  const bannerTranslateY = useSharedValue(16);
  const cardOpacity = useSharedValue(0);
  const cardTranslateY = useSharedValue(16);

  useFocusEffect(
    useCallback(() => {
      bannerOpacity.value = 0;
      bannerTranslateY.value = 16;
      cardOpacity.value = 0;
      cardTranslateY.value = 16;

      bannerOpacity.value = withTiming(1, { duration: 400 });
      bannerTranslateY.value = withTiming(0, { duration: 400 });
      cardOpacity.value = withDelay(150, withTiming(1, { duration: 400 }));
      cardTranslateY.value = withDelay(150, withTiming(0, { duration: 400 }));
    }, [])
  );

  const bannerStyle = useAnimatedStyle(() => ({
    opacity: bannerOpacity.value,
    transform: [{ translateY: bannerTranslateY.value }],
  }));

  const cardStyle = useAnimatedStyle(() => ({
    opacity: cardOpacity.value,
    transform: [{ translateY: cardTranslateY.value }],
  }));

  const { isSurviving, currentRemaining, totalBudget, chartData } = data;
  const accentColor = isSurviving ? '#22C55E' : '#EF4444';
  const gradientId = isSurviving ? 'greenGradient' : 'redGradient';
  const chartInnerHeight = CHART_HEIGHT - CHART_LABEL_HEIGHT;
  const totalDays = chartData.length;

  const toX = (i: number) =>
    totalDays <= 1 ? 0 : (i / (totalDays - 1)) * CHART_WIDTH;
  const toY = (value: number) =>
    chartInnerHeight - (value / totalBudget) * chartInnerHeight;

  const idealPoints = chartData.map((d, i) => ({ x: toX(i), y: toY(d.idealRemaining) }));
  const actualPoints = chartData
    .map((d, i) =>
      d.actualRemaining !== null ? { x: toX(i), y: toY(d.actualRemaining) } : null
    )
    .filter((p): p is { x: number; y: number } => p !== null);

  const idealLinePath = toLinePath(idealPoints);
  const actualLinePath = toLinePath(actualPoints);
  const fillPath =
    actualPoints.length > 0
      ? `${actualLinePath} L ${actualPoints[actualPoints.length - 1].x.toFixed(1)} ${chartInnerHeight} L ${actualPoints[0].x.toFixed(1)} ${chartInnerHeight} Z`
      : '';
  const lastActualPoint = actualPoints[actualPoints.length - 1];

  const weekLabels = ['WEEK 1', 'WEEK 2', 'WEEK 3', 'WEEK 4'];
  const weekXPositions = [0.125, 0.375, 0.625, 0.875].map((r) => r * CHART_WIDTH);

  const vsSign = vsIdealPercent >= 0 ? '+' : '';
  const vsText = `이상잔액 대비 ${vsSign}${Math.round(vsIdealPercent)}%`;
  const badgeBg = isSurviving ? '#DCFCE7' : '#FEE2E2';

  return (
    <View style={styles.container}>
      {/* 상태 배너 */}
      <Animated.View style={[styles.banner, bannerStyle]}>
        <Text style={styles.bannerEmoji}>{isSurviving ? '🚀' : '⚠️'}</Text>
        <Text style={styles.bannerText}>
          {isSurviving
            ? '잘 버티고 있어요! 지갑 심폐소생술 중'
            : daysUntilBankrupt != null
              ? `이 속도라면 ${daysUntilBankrupt}일 후 파산입니다!`
              : '이 속도라면 파산 위험입니다!'}
        </Text>
      </Animated.View>

      {/* 카드 */}
      <Animated.View style={[styles.card, cardStyle]}>
        <View style={styles.cardHeader}>
          <View>
            <Text style={styles.amountText}>{formatWon(currentRemaining)}</Text>
            <View style={styles.badgeRow}>
              <View style={[styles.badge, { backgroundColor: badgeBg }]}>
                <Text style={[styles.badgeText, { color: accentColor }]}>{vsText}</Text>
              </View>
              <Text style={styles.periodText}>{getMonthLabel(yearMonth)}</Text>
            </View>
          </View>
          <TouchableOpacity style={styles.calendarButton} onPress={onToggleView}>
            <FontAwesome5 name="calendar-alt" size={18} color="#6B7280" />
          </TouchableOpacity>
        </View>

        <View style={styles.chartContainer}>
          <Svg width={CHART_WIDTH} height={CHART_HEIGHT}>
            <Defs>
              <LinearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
                <Stop offset="0" stopColor={accentColor} stopOpacity="0.25" />
                <Stop offset="1" stopColor={accentColor} stopOpacity="0" />
              </LinearGradient>
            </Defs>

            {fillPath ? <Path d={fillPath} fill={`url(#${gradientId})`} /> : null}

            <Path
              d={idealLinePath}
              stroke="#9CA3AF"
              strokeWidth="1.5"
              strokeDasharray="5 4"
              fill="none"
            />

            {actualLinePath ? (
              <Path d={actualLinePath} stroke={accentColor} strokeWidth="2" fill="none" />
            ) : null}

            {lastActualPoint && (
              <Circle cx={lastActualPoint.x} cy={lastActualPoint.y} r="4" fill={accentColor} />
            )}

            {weekLabels.map((label, i) => (
              <SvgText
                key={label}
                x={weekXPositions[i]}
                y={CHART_HEIGHT - 4}
                fontSize="10"
                fill="#9CA3AF"
                textAnchor="middle"
              >
                {label}
              </SvgText>
            ))}
          </Svg>
        </View>
      </Animated.View>
    </View>
  );
}
