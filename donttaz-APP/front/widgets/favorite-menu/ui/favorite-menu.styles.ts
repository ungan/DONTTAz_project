import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  card: {
    width: 350,
    borderRadius: 16,
    backgroundColor: '#FFF',
    paddingVertical: 16,
    paddingHorizontal: 24,
    flexDirection: 'column',
    alignItems: 'center',
    gap: 20,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 25,
  },
  item: {
    alignItems: 'center',
    gap: 8,
  },
  label: {
    fontSize: 12,
    fontWeight: '500',
    color: '#344054',
    fontFamily: 'Roboto',
  },
});
