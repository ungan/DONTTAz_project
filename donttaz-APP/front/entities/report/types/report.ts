export interface SpendingReportCategoryItem {
  categoryName: string;
  allocatedAmount: number;
  spentAmount: number;
  categoryRate: number;
  budgetAchievementRate?: number;
  feedback: string;
}

export interface SpendingReportFeedbackItem {
  type: 'persona' | 'warning' | 'pattern' | 'positive';
  categoryName: string | null;
  message: string;
}

export interface SpendingReportData {
  yearMonth: string;
  sealedAmount: number;
  totalBudget: number;
  totalSpent: number;
  achievementRate: number;
  spendingPersona: string | null;
  aiOverallFeedback: string;
  categoryReports: SpendingReportCategoryItem[];
  personalizedFeedback: SpendingReportFeedbackItem[];
}

export interface MonthlyReportResponse {
  status: string;
  message: string;
  data: SpendingReportData | null;
}

export interface MonthlyReportParams {
  yearMonth: string;
}
