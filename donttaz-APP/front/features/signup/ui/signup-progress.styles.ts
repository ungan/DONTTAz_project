import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    gap: 12,
  },
  labels: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 8,
  },
  label: {
    flex: 1,
    fontSize: 12,
    fontWeight: '500',
    color: '#9CA3AF',
    textAlign: 'center',
  },
  activeLabel: {
    color: '#111827',
  },
  bars: {
    flexDirection: 'row',
    gap: 8,
  },
  bar: {
    flex: 1,
    height: 6,
    borderRadius: 999,
    backgroundColor: '#E5E7EB',
  },
  activeBar: {
    backgroundColor: Primary,
  },
});
