import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    paddingHorizontal: 24,
    gap: 8,
  },
  quickButton: {
    flex: 1,
    height: 40,
    borderRadius: 999,
    backgroundColor: '#F3F4F6',
    alignItems: 'center',
    justifyContent: 'center',
  },
  quickButtonLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
  },
});
