import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 16,
    paddingTop: 8,
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
