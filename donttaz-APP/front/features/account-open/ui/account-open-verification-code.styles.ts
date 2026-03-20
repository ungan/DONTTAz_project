import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 12,
  },
  box: {
    width: 40,
    height: 48,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#D1D5DB',
    backgroundColor: '#F9FAFB',
  },
  boxFilled: {
    borderColor: Primary,
    backgroundColor: '#DCEBFF',
  },
});
