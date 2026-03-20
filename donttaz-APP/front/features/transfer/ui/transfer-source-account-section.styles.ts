import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    gap: 12,
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
  },
  helper: {
    fontSize: 14,
    color: '#6B7280',
    lineHeight: 20,
  },
  cardList: {
    gap: 12,
  },
  card: {
    borderWidth: 1,
    borderColor: '#E5E7EB',
    borderRadius: 16,
    backgroundColor: '#FFFFFF',
    padding: 16,
    gap: 10,
  },
  selectedCard: {
    borderColor: Primary,
    backgroundColor: '#F8FBFF',
  },
  unavailableCard: {
    opacity: 0.55,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: 12,
  },
  bankName: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111827',
  },
  badge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 999,
    backgroundColor: '#E8F1FF',
  },
  badgeText: {
    fontSize: 12,
    fontWeight: '600',
    color: Primary,
  },
  accountName: {
    fontSize: 15,
    fontWeight: '600',
    color: '#374151',
  },
  accountNumber: {
    fontSize: 14,
    color: '#6B7280',
  },
  balance: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111827',
  },
  emptyText: {
    fontSize: 14,
    color: '#6B7280',
    lineHeight: 20,
  },
  warningText: {
    fontSize: 13,
    color: '#DC2626',
    lineHeight: 18,
  },
});
