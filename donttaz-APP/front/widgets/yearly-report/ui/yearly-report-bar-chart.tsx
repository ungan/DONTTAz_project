import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, LayoutChangeEvent } from 'react-native';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withTiming,
  Easing,
  SharedValue,
} from 'react-native-reanimated';
import Svg, { Line } from 'react-native-svg';
import { MonthlyExpenditure } from '@/entities/report';
import { styles } from './yearly-report-bar-chart.styles';

const CHART_HEIGHT = 160;
const GRID_LINES = 4;
const BAR_RADIUS = 4;
const PRIMARY = '#5899FD';
const SELECTED_COLOR = '#0055B2';
const EMPTY_COLOR = '#E5E7EB';

const MONTH_LABELS = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];

interface BarProps {
  barHeight: number;
  barColor: string;
  progress: SharedValue<number>;
  isEmpty: boolean;
  onPress: () => void;
}

function AnimatedBar({ barHeight, barColor, progress, isEmpty, onPress }: BarProps) {
  const animatedStyle = useAnimatedStyle(() => ({
    height: barHeight * progress.value,
  }));

  return (
    <TouchableOpacity
      style={{ flex: 1, alignItems: 'center', height: CHART_HEIGHT, justifyContent: 'flex-end' }}
      onPress={onPress}
      activeOpacity={isEmpty ? 1 : 0.7}
    >
      <Animated.View
        style={[
          {
            width: '60%',
            backgroundColor: barColor,
            borderRadius: BAR_RADIUS,
          },
          animatedStyle,
        ]}
      />
    </TouchableOpacity>
  );
}

interface Props {
  data: MonthlyExpenditure[];
  selectedMonth: string | null;
  onBarPress: (month: string) => void;
}

export function YearlyReportBarChart({ data, selectedMonth, onBarPress }: Props) {
  const [chartWidth, setChartWidth] = useState(0);
  const progress = useSharedValue(0);

  const maxAmount = Math.max(...data.map((d) => d.amount), 1);
  const roundedMax = Math.ceil(maxAmount / 100000) * 100000;

  const yLabels = Array.from({ length: GRID_LINES + 1 }, (_, i) =>
    Math.round((roundedMax / GRID_LINES) * (GRID_LINES - i))
  );

  const formatYLabel = (value: number) => {
    if (value >= 10000) return `${Math.round(value / 10000)}만`;
    return `${value}`;
  };

  useEffect(() => {
    progress.value = 0;
    progress.value = withTiming(1, {
      duration: 600,
      easing: Easing.out(Easing.cubic),
    });
  }, [data]);

  const handleLayout = (e: LayoutChangeEvent) => {
    setChartWidth(e.nativeEvent.layout.width);
  };

  return (
    <View style={styles.container}>
      <View style={styles.chartWrapper}>
        {/* Y축 */}
        <View style={[styles.yAxis, { height: CHART_HEIGHT }]}>
          {yLabels.map((label, i) => (
            <Text
              key={i}
              style={[styles.yLabel, { position: 'absolute', top: (CHART_HEIGHT / GRID_LINES) * i - 6 }]}
            >
              {formatYLabel(label)}
            </Text>
          ))}
        </View>

        {/* 바 차트 */}
        <View style={styles.chart} onLayout={handleLayout}>
          {chartWidth > 0 && (
            <Svg width={chartWidth} height={CHART_HEIGHT}>
              {yLabels.map((_, i) => (
                <Line
                  key={i}
                  x1={0}
                  y1={(CHART_HEIGHT / GRID_LINES) * i}
                  x2={chartWidth}
                  y2={(CHART_HEIGHT / GRID_LINES) * i}
                  stroke="#F3F4F6"
                  strokeWidth={1}
                />
              ))}
            </Svg>
          )}

          <View
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              flexDirection: 'row',
              alignItems: 'flex-end',
            }}
          >
            {data.map((item) => {
              const isSelected = selectedMonth === item.month;
              const isEmpty = item.amount === 0;
              const barHeightRatio = isEmpty ? 0.02 : item.amount / roundedMax;
              const barHeight = Math.max(barHeightRatio * CHART_HEIGHT, isEmpty ? 4 : 8);
              const barColor = isEmpty ? EMPTY_COLOR : isSelected ? SELECTED_COLOR : PRIMARY;

              return (
                <AnimatedBar
                  key={item.month}
                  barHeight={barHeight}
                  barColor={barColor}
                  progress={progress}
                  isEmpty={isEmpty}
                  onPress={() => !isEmpty && onBarPress(item.month)}
                />
              );
            })}
          </View>
        </View>
      </View>

      {/* X축 라벨 */}
      <View style={styles.xAxis}>
        {data.map((item) => (
          <Text
            key={item.month}
            style={[styles.xLabel, selectedMonth === item.month && styles.xLabelSelected]}
          >
            {MONTH_LABELS[parseInt(item.month, 10) - 1]}
          </Text>
        ))}
      </View>
    </View>
  );
}
