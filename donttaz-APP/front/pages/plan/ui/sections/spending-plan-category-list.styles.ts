import { StyleSheet } from 'react-native';

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
  title: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
  },
  item: {
    marginTop: 14,
    borderRadius: 16,
    backgroundColor: '#F8FAFC',
    padding: 14,
  },
  topRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  dot: {
    width: 10,
    height: 10,
    borderRadius: 999,
  },
  category: {
    flex: 1,
    marginLeft: 10,
    fontSize: 15,
    fontWeight: '700',
    color: '#111827',
  },
  amount: {
    fontSize: 15,
    fontWeight: '700',
    color: '#111827',
  },
  comment: {
    marginTop: 10,
    fontSize: 13,
    lineHeight: 19,
    color: '#6B7280',
  },
});
