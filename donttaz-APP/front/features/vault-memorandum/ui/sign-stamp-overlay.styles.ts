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
    width: 300,
    height: 100,
    borderRadius: 8,
    borderWidth: 6,
    borderColor: '#C0392B',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'transparent',
  },
  stampInner: {
    width: 264,
    height: 84,
    borderRadius: 4,
    borderWidth: 2,
    borderColor: '#C0392B',
    alignItems: 'center',
    justifyContent: 'center',
  },
  stampText: {
    fontSize: 40,
    color: '#C0392B',
    letterSpacing: 8,
    fontFamily: 'BebasNeue',
  },
});
