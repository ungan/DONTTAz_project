import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    width: 220,
    height: 220,
    alignSelf: 'center',
    alignItems: 'center',
    justifyContent: 'center',
  },
  centerLabel: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
  },
  centerText: {
    fontSize: 20,
    fontWeight: '700',
    color: '#111827',
  },
  percentLabel: {
    position: 'absolute',
    fontSize: 11,
    fontWeight: '500',
    color: '#6B7280',
    width: 32,
    textAlign: 'center',
  },
});
