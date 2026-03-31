export type BudgetStrategy = 'safe' | 'balanced' | 'tight' | 'aggressive' | string;

export interface SpendingPlanCategoryPlanResponse {
  categoryName: string;
  allocatedAmount: number;
  aiComment?: string | null;
}

export interface SpendingPlanRecommendationResponse {
  type: string;
  categoryName: string | null;
  message: string;
}

export interface SpendingPlanResponseData {
  planId?: number;
  targetYearMonth: string;
  referenceStartMonth?: string | null;
  referenceEndMonth?: string | null;
  totalBudget: number;
  sealedAmount?: number;
  surplusAmount?: number;
  paydayDay?: number | null;
  riskScore?: number | null;
  budgetStrategy?: BudgetStrategy | null;
  categoryPlans: SpendingPlanCategoryPlanResponse[];
  personalizedRecommendations?: SpendingPlanRecommendationResponse[];
  spendingPersona?: string | null;
}

export interface SpendingPlanResponse {
  status: string;
  message: string;
  data: SpendingPlanResponseData;
}

export interface SpendingPlanCategoryPlan {
  categoryName: string;
  allocatedAmount: number;
  aiComment: string;
  percentage: number;
  color: string;
}

export interface SpendingPlanRecommendation {
  type: string;
  categoryName: string | null;
  message: string;
  typeLabel: string;
  accentColor: string;
  backgroundColor: string;
}

export interface SpendingPlanData {
  planId?: number;
  targetYearMonth: string;
  referenceStartMonth: string | null;
  referenceEndMonth: string | null;
  totalBudget: number;
  sealedAmount: number;
  surplusAmount: number;
  paydayDay: number | null;
  riskScore: number | null;
  riskPercent: number | null;
  riskLevelLabel: string;
  budgetStrategy: BudgetStrategy | null;
  budgetStrategyLabel: string;
  spendingPersona: string | null;
  categoryPlans: SpendingPlanCategoryPlan[];
  personalizedRecommendations: SpendingPlanRecommendation[];
  totalAllocatedAmount: number;
}
