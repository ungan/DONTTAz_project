export interface BurndownChartItem {
  date: string;
  idealRemaining: number;
  actualRemaining: number | null;
}

export interface BurndownData {
  targetYearMonth: string;
  totalBudget: number;
  currentRemaining: number;
  isSurviving: boolean;
  chartData: BurndownChartItem[];
}

export interface BurndownResponse {
  status: string;
  message: string;
  data: BurndownData;
}
