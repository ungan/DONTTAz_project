import { useMemo } from 'react';
import type { BurndownData } from './types';

export interface BurndownStats {
  vsIdealPercent: number;   // 실제 vs 이상 잔액 % 차이 (양수: 절약, 음수: 초과)
  daysUntilBankrupt: number | null; // isSurviving=false 일 때만 계산
}

export function useBurndownStats(data: BurndownData | null): BurndownStats {
  return useMemo(() => {
    if (!data || data.chartData.length === 0) {
      return { vsIdealPercent: 0, daysUntilBankrupt: null };
    }

    // 실제 데이터가 있는 마지막 항목
    const actualItems = data.chartData.filter((d) => d.actualRemaining !== null);
    if (actualItems.length === 0) {
      return { vsIdealPercent: 0, daysUntilBankrupt: null };
    }

    const latest = actualItems[actualItems.length - 1];
    const latestActual = latest.actualRemaining as number;

    const vsIdealPercent =
      latest.idealRemaining !== 0
        ? ((latestActual - latest.idealRemaining) / latest.idealRemaining) * 100
        : 0;

    if (data.isSurviving) {
      return { vsIdealPercent, daysUntilBankrupt: null };
    }

    // 일평균 소비 = (첫날 잔액 - 마지막 실제 잔액) / 경과 일수
    const firstActual = actualItems[0].actualRemaining as number;
    const elapsedDays = actualItems.length - 1;
    const dailyBurn = elapsedDays > 0 ? (firstActual - latestActual) / elapsedDays : 0;

    const daysUntilBankrupt =
      dailyBurn > 0 ? Math.ceil(data.currentRemaining / dailyBurn) : null;

    return { vsIdealPercent, daysUntilBankrupt };
  }, [data]);
}
