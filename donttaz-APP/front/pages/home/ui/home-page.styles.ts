import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  scrollView: {
    flex: 1,
    backgroundColor: Primary,
  },
  container: {
    flexGrow: 1,
  },
  topSection: {
    paddingTop: 70,
    paddingHorizontal: 16,
    alignItems: 'center',
    gap: 16,
    zIndex: 1,
  },
  whiteCircle: {
    position: 'absolute',
    width: 900,
    height: 900,
    borderRadius: 450,
    backgroundColor: '#F5F7FA',
    alignSelf: 'center',
    top: 300,
    zIndex: 0,
  },
  widgetsSection: {
    alignItems: 'center',
    paddingTop: 32,
    paddingBottom: 32,
    gap: 16,
    zIndex: 1,
    marginTop: -8,
  },
});
