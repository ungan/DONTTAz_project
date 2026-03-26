import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  card: {
    borderRadius: 20,
    backgroundColor: '#FFFFFF',
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 10,
    elevation: 2,
  },
  badge: {
    alignSelf: 'flex-start',
    borderRadius: 999,
    backgroundColor: '#EEF4FF',
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
  badgeText: {
    fontSize: 12,
    fontWeight: '700',
    color: Primary,
  },
  title: {
    marginTop: 12,
    fontSize: 24,
    fontWeight: '800',
    color: '#111827',
  },
  subtitle: {
    marginTop: 8,
    fontSize: 14,
    lineHeight: 20,
    color: '#6B7280',
  },
  totalLabel: {
    marginTop: 20,
    fontSize: 13,
    fontWeight: '600',
    color: '#6B7280',
  },
  totalAmount: {
    marginTop: 6,
    fontSize: 30,
    fontWeight: '800',
    color: '#111827',
  },
  statRow: {
    marginTop: 18,
    flexDirection: 'row',
  },
  statCard: {
    flex: 1,
    borderRadius: 16,
    backgroundColor: '#F8FAFC',
    paddingVertical: 16,
    paddingHorizontal: 14,
  },
  statSpacer: {
    width: 12,
  },
  statLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#6B7280',
  },
  statValue: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
  },
  footnote: {
    marginTop: 14,
    fontSize: 13,
    lineHeight: 19,
    color: '#6B7280',
  },
});
