import { StyleSheet } from 'react-native';

import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    backgroundColor: '#fff',
    borderTopWidth: 1,
    borderTopColor: '#E8E8E8',
    paddingTop: 10,
  },
  tab: {
    flex: 1,
    alignItems: 'center',
    gap: 4,
  },
  label: {
    fontSize: 11,
    color: '#9BA1A6',
  },
  labelFocused: {
    color: Primary,
    fontWeight: '600',
  },
});
