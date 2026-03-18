import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    paddingHorizontal: 20,
  },
  tab: {
    flex: 1,
    alignItems: 'center',
    paddingBottom: 10,
    borderBottomWidth: 4,
    borderBottomColor: '#E5E7EB',
  },
  tabActive: {
    borderBottomColor: Primary,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    color: '#D1D5DB',
  },
  labelActive: {
    color: '#111827',
  },
});
