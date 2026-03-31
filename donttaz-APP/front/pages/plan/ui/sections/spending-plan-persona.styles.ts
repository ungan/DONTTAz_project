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
  persona: {
    marginTop: 14,
    fontSize: 24,
    fontWeight: '800',
    color: '#111827',
  },
  helper: {
    marginTop: 6,
    fontSize: 13,
    color: '#6B7280',
  },
  riskRow: {
    marginTop: 16,
    flexDirection: 'row',
    alignItems: 'center',
  },
  riskCaption: {
    fontSize: 13,
    fontWeight: '600',
    color: '#6B7280',
  },
  riskDot: {
    fontSize: 13,
    color: '#D1D5DB',
  },
  riskLabel: {
    fontSize: 13,
    fontWeight: '700',
  },
  riskValue: {
    fontSize: 13,
    fontWeight: '700',
    color: '#111827',
  },
});
