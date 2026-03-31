import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  errorText: {
    fontSize: 14,
    color: '#EF4444',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 4,
  },
  totalLabel: {
    fontSize: 13,
    color: '#6B7280',
    marginBottom: 4,
  },
  totalAmount: {
    fontSize: 26,
    fontWeight: '700',
    color: '#111827',
  },
  yearSelector: {
    width: 110,
  },
});
