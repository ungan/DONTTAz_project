import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    width: '100%',
    gap: 8,
  },
  stepItem: {
    flex: 1,
    gap: 8,
  },
  stepLabel: {
    fontSize: 12,
    fontWeight: '500',
    color: '#9CA3AF',
    textAlign: 'center',
  },
  stepLabelActive: {
    color: '#111827',
  },
  stepBar: {
    height: 3,
    borderRadius: 999,
    backgroundColor: '#E5E7EB',
  },
  stepBarActive: {
    backgroundColor: Primary,
  },
});
