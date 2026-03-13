import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 200,
    paddingHorizontal: 16,
    paddingBottom: 0,
    flexDirection: 'column',
    alignItems: 'center',
    gap: 48,
    backgroundColor: '#FFFFFF',
  },
  header: {
    alignItems: 'center',
    gap: 8,
  },
  title: {
    color: '#000000',
    textAlign: 'center',
    fontFamily: 'Roboto',
    fontSize: 40,
    fontWeight: '700',
  },
  subtitle: {
    color: '#000000',
    textAlign: 'center',
    fontFamily: 'Roboto',
    fontSize: 17,
    fontWeight: '400',
  },
});
