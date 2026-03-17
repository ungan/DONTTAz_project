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
    borderRadius: 8,
    borderWidth: 1,
    borderColor: Primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  quickButtonLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: Primary,
  },
});
