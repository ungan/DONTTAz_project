import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 20,
    paddingTop: 24,
    paddingBottom: 8,
  },
  title: {
    fontSize: 20,
    fontWeight: '700',
    color: Primary,
    marginBottom: 10,
    textAlign: 'center',
  },
  message: {
    fontSize: 13.5,
    fontWeight: '400',
    color: '#111827',
    lineHeight: 22,
    textAlign: 'center',
  },
});
