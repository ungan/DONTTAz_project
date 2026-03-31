import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    width: 335,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: 130,
    minHeight: 66,
  },
  textGroup: {
    gap: 0,
  },
  userName: {
    color: '#FFFFFF',
    fontFamily: 'Roboto',
    fontSize: 21,
    // fontWeight: '400',
    lineHeight: 28,
  },
  userNameBold: {
    fontWeight: '600',
  },
  subText: {
    fontSize: 18,
    fontWeight: '400',
    color: 'rgba(255, 255, 255, 0.65)',
    lineHeight: 28,
  },
});
