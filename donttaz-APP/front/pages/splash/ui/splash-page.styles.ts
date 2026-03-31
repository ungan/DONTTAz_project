import { StyleSheet } from 'react-native';

import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingHorizontal: 16,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Primary,
  },
  contentWrapper: {
    flexDirection: 'column',
    alignItems: 'center',
  },
  mascot: {
    width: 263,
    height: 211,
  },
  title: {
    color: '#FFF',
    textAlign: 'center',
    fontFamily: 'BalooDa2Bold',
    fontSize: 75,
    fontWeight: '800',
    letterSpacing: 0.07,
  },
});
