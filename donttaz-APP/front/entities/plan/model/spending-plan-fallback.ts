import type { SpendingPlanResponse } from './plan-types';

export function getFallbackSpendingPlanResponse(targetYearMonth: string): SpendingPlanResponse {
  return {
    status: 'SUCCESS',
    message: '플랜 생성 완료',
    data: {
      targetYearMonth,
      referenceStartMonth: null,
      referenceEndMonth: null,
      totalBudget: 3967809,
      sealedAmount: 750000,
      surplusAmount: 0,
      paydayDay: 24,
      riskScore: 0.36,
      budgetStrategy: 'safe',
      categoryPlans: [
        {
          categoryName: '식비',
          allocatedAmount: 1142045,
          aiComment: '식비 예산을 배정했습니다.',
        },
        {
          categoryName: '교통',
          allocatedAmount: 728365,
          aiComment:
            '이번달 교통비가 12,131원으로 전달 대비 41.3% 증가하여 권장 비중을 초과했습니다.',
        },
        {
          categoryName: '여가',
          allocatedAmount: 687769,
          aiComment: '이번달 여가비가 19,158원으로 권장 비중을 초과했습니다.',
        },
        {
          categoryName: '기타',
          allocatedAmount: 659308,
          aiComment: '기타 예산을 배정했습니다.',
        },
        {
          categoryName: '쇼핑',
          allocatedAmount: 522635,
          aiComment: '쇼핑 예산을 배정했습니다.',
        },
        {
          categoryName: '기타',
          allocatedAmount: 227687,
          aiComment: '기타 예산을 배정했습니다.',
        },
      ],
      personalizedRecommendations: [
        {
          type: 'warning',
          categoryName: '교통',
          message:
            '교통비가 권장 비중을 초과했습니다. 대중교통 이용을 늘리고 차량 이용을 줄이는 방법으로 절감해보세요.',
        },
        {
          type: 'warning',
          categoryName: '여가',
          message: '여가비가 권장 비중을 초과하였습니다. 무료 또는 저렴한 여가 활동을 찾아보세요.',
        },
        {
          type: 'warning',
          categoryName: '생활',
          message: '생활비가 너무 높습니다. 필요 없는 지출을 점검하고 줄이도록 해보세요.',
        },
        {
          type: 'positive',
          categoryName: '식비',
          message: '식비가 전달 대비 100% 감소하여 잘 관리하고 있습니다. 이 상태를 유지해주세요.',
        },
        {
          type: 'habit_change',
          categoryName: null,
          message: '이번달 소비 성향이 식도락형에서 생활밀착형으로 변화했습니다. 앞으로의 소비 패턴에 주의하세요.',
        },
      ],
      spendingPersona: '균형소비형',
    },
  };
}
