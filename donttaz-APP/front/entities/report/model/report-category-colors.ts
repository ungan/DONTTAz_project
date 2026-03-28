const CATEGORY_COLORS: Record<string, string> = {
  '식비':       '#F97316',
  '식비/카페':  '#FBBF24',
  '교통':       '#5899FD',
  '여가':       '#A855F7',
  '문화/여가':  '#A855F7',
  '통신/보험':  '#10B981',
  '금융/보험':  '#06B6D4',
  '쇼핑':       '#F472B6',
  '송금':       '#EF4444',
  '의료/건강':  '#34D399',
  '미분류':     '#CBD5E1',
  '기타':       '#94A3B8',
};

const DEFAULT_COLOR = '#64748B';

export function getReportCategoryColor(category: string): string {
  return CATEGORY_COLORS[category] ?? DEFAULT_COLOR;
}
