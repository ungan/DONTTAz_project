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
  title: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
  },
  row: {
    marginTop: 16,
    flexDirection: 'row',
  },
  left: {
    flex: 1.2,
  },
  right: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'flex-end',
  },
  label: {
    fontSize: 12,
    fontWeight: '600',
    color: '#6B7280',
  },
  persona: {
    marginTop: 6,
    fontSize: 24,
    fontWeight: '800',
    color: '#111827',
  },
  helper: {
    marginTop: 8,
    fontSize: 13,
    lineHeight: 19,
    color: '#6B7280',
  },
  riskBadge: {
    borderRadius: 999,
    backgroundColor: '#EEF4FF',
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
  riskBadgeText: {
    fontSize: 12,
    fontWeight: '700',
    color: Primary,
  },
  riskValue: {
    marginTop: 10,
    fontSize: 22,
    fontWeight: '800',
    color: '#111827',
  },
  riskCaption: {
    marginTop: 6,
    fontSize: 13,
    color: '#6B7280',
  },
});
