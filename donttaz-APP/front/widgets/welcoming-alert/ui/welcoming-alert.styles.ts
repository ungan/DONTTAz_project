import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    width: 335,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: 130,
  },
  textGroup: {
    gap: 16,
  },
  greeting: {
    color: '#C7D7FE',
    fontFamily: 'Roboto',
    fontSize: 14,
    fontWeight: '500',
    lineHeight: 20,
  },
  userName: {
    color: '#FFFFFF',
    fontFamily: 'Roboto',
    fontSize: 20,
    fontWeight: '600',
    lineHeight: 30,
  },
  notificationBox: {
    width: 46,
    height: 48,
    paddingVertical: 16,
    paddingHorizontal: 15,
    justifyContent: 'center',
    alignItems: 'center',
    flexShrink: 0,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.20)',
  },
});
