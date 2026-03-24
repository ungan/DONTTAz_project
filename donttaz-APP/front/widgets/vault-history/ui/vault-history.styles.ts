import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 16,
    paddingBottom: 32,
    marginTop: 8,
    borderTopWidth: 1,
    borderTopColor: '#F3F4F6',
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: '#111827',
    paddingTop: 20,
    paddingBottom: 4,
  },
  emptyContainer: {
    paddingVertical: 40,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 14,
    color: '#9CA3AF',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 16,
  },
  date: {
    width: 44,
    fontSize: 13,
    color: '#9CA3AF',
  },
  label: {
    flex: 1,
    fontSize: 14,
    color: '#111827',
    marginLeft: 12,
  },
  amount: {
    fontSize: 14,
    color: '#111827',
  },
  amountDeposit: {
    fontWeight: '700',
    color: '#5899FD',
  },
  separator: {
    height: 1,
    backgroundColor: '#F3F4F6',
  },
});
