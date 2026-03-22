import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  overlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0, 0, 0, 0.82)',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 100,
  },
  stamp: {
    width: 160,
    height: 160,
    borderRadius: 8,
    borderWidth: 6,
    borderColor: '#C0392B',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'transparent',
  },
  stampInner: {
    width: 144,
    height: 144,
    borderRadius: 4,
    borderWidth: 2,
    borderColor: '#C0392B',
    alignItems: 'center',
    justifyContent: 'center',
  },
  stampText: {
    fontSize: 72,
    fontWeight: '900',
    color: '#C0392B',
    letterSpacing: 4,
    fontFamily: 'serif',
  },
});
