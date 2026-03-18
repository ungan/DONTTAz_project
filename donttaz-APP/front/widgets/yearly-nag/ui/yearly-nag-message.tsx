import React from 'react';
import { View, Text } from 'react-native';
import { styles } from './yearly-nag-message.styles';

interface NagContent {
  emoji: string;
  message: string;
}

const NAG_MAP: Record<string, NagContent> = {
  '식비/카페': {
    emoji: '☕',
    message: '제 해바라기씨 1년 치를 하루 만에 다 드시면 어떡해요! 🐭',
  },
  '교통': {
    emoji: '🚌',
    message: '전 네 발로 공짜로 뛰는데, 어딜 그렇게 돈 내고 가셨어요? 🐭',
  },
  '문화/여가': {
    emoji: '🎬',
    message: '방구석 쳇바퀴가 무료인데 밖에서 돈을 왜 쓰세요? 🐭',
  },
  '통신/보험': {
    emoji: '📱',
    message: '데이터 요금 낼 돈으로 제 해바라기씨나 더 사주시지... 🐭',
  },
  '쇼핑': {
    emoji: '🛍️',
    message: '전 털옷 한 벌로 평생 사는데, 택배가 또 왔네요? 🐭',
  },
  '송금': {
    emoji: '💸',
    message: '제 간식 살 돈은 없으면서 남한테는 완전 천사시네요? 🐭',
  },
};

const DEFAULT_NAG: NagContent = {
  emoji: '💰',
  message: '땃쥐도 놀란 주인님의 지출 내역이에요! 🐭',
};

interface Props {
  year: string;
  topCategory: string;
}

export function YearlyNagMessage({ year, topCategory }: Props) {
  const nag = NAG_MAP[topCategory] ?? DEFAULT_NAG;

  return (
    <View style={styles.container}>
      <Text style={styles.title}>
        이번 해 지출 TOP 1은 {topCategory} {nag.emoji}
      </Text>
      <Text style={styles.message}>{nag.message}</Text>
    </View>
  );
}
