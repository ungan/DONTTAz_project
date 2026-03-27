const CATEGORY_COLORS: Record<string, string> = {
  '\uC2DD\uBE44': '#5899FD',
  '\uC2DD\uBE44/\uCE74\uD398': '#5899FD',
  '\uAD50\uD1B5': '#FF6B6B',
  '\uC5EC\uAC00': '#FFD93D',
  '\uBB38\uD654/\uC5EC\uAC00': '#FFD93D',
  '\uD1B5\uC2E0/\uBCF4\uD5D8': '#6BCB77',
  '\uAE08\uC735/\uBCF4\uD5D8': '#6BCB77',
  '\uC1FC\uD551': '#C77DFF',
  '\uC1A1\uAE08': '#FF9F43',
  '\uAE30\uD0C0': '#94A3B8',
};

const DEFAULT_COLOR = '#A0AEC0';

export function getReportCategoryColor(category: string): string {
  return CATEGORY_COLORS[category] ?? DEFAULT_COLOR;
}
