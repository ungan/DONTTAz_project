import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 24,
    backgroundColor: '#FFFFFF',
    gap: 24,
  },
  heroSection: {
    gap: 8,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    lineHeight: 32,
    color: '#111827',
  },
  description: {
    fontSize: 15,
    lineHeight: 22,
    color: '#6B7280',
  },
  caption: {
    fontSize: 13,
    lineHeight: 20,
    color: '#9CA3AF',
  },
  summaryCard: {
    gap: 12,
    padding: 16,
    borderRadius: 16,
    backgroundColor: '#F9FAFB',
    borderWidth: 1,
    borderColor: '#E5E7EB',
  },
  summaryTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111827',
  },
  summaryRow: {
    gap: 4,
  },
  summaryLabel: {
    fontSize: 13,
    color: '#6B7280',
  },
  summaryValue: {
    fontSize: 15,
    fontWeight: '600',
    color: '#111827',
  },
  errorText: {
    fontSize: 13,
    color: '#EF4444',
  },
  helperText: {
    fontSize: 13,
    lineHeight: 20,
    color: '#6B7280',
  },
});
