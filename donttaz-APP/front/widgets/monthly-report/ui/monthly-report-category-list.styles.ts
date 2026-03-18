import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    marginHorizontal: 20,
    marginTop: 8,
    borderRadius: 16,
    backgroundColor: '#FFFFFF',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.06,
    shadowRadius: 4,
    elevation: 2,
    paddingVertical: 8,
  },
  item: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 10,
    gap: 12,
  },
  info: {
    flex: 1,
  },
  category: {
    fontSize: 14,
    color: '#111827',
  },
  percentage: {
    fontSize: 13,
    fontWeight: '700',
    color: '#374151',
    marginTop: 2,
  },
  amount: {
    fontSize: 14,
    color: '#374151',
  },
});
