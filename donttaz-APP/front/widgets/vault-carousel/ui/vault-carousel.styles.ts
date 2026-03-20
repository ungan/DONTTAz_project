import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  dotContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 6,
    paddingVertical: 12,
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: '#D1D5DB',
  },
  dotActive: {
    width: 18,
    backgroundColor: Primary,
    borderRadius: 3,
  },
});
