import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    gap: 10,
    padding: 16,
    backgroundColor: '#F3F4F6',
    borderRadius: 12,
    margin: 16,
  },
  emoji: {
    fontSize: 24,
  },
  textContainer: {
    flex: 1,
    gap: 4,
  },
  text: {
    fontSize: 13,
    color: '#6B7280',
    lineHeight: 18,
  },
  bold: {
    fontWeight: '700',
    fontSize: 14.5,
    color: '#5899FD',
  },
});
