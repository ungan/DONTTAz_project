import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    gap: 10,
  },
  title: {
    fontSize: 13,
    fontWeight: '600',
    color: '#111827',
    letterSpacing: 0.3,
  },
  cardList: {
    gap: 10,
  },
  card: {
    borderWidth: 1.5,
    borderColor: '#F3F4F6',
    borderRadius: 16,
    backgroundColor: '#FAFAFA',
    padding: 16,
    gap: 6,
  },
  selectedCard: {
    borderColor: Primary,
    backgroundColor: '#F0F6FF',
  },
  unavailableCard: {
    opacity: 0.45,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  bankName: {
    fontSize: 15,
    fontWeight: '700',
    color: '#111827',
  },
  badge: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 999,
    backgroundColor: '#E8F1FF',
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '600',
    color: Primary,
  },
  accountName: {
    fontSize: 13,
    color: '#6B7280',
  },
  accountNumber: {
    fontSize: 13,
    color: '#9CA3AF',
  },
  balance: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
    marginTop: 4,
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
