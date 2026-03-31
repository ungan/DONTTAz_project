import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 20,
    marginTop: 16,
  },
  chartWrapper: {
    flexDirection: 'row',
    alignItems: 'flex-end',
  },
  yAxis: {
    width: 40,
    alignItems: 'flex-end',
    paddingRight: 8,
  },
  yLabel: {
    fontSize: 10,
    color: '#9CA3AF',
  },
  chart: {
    flex: 1,
  },
  xAxis: {
    flexDirection: 'row',
    marginTop: 6,
    paddingLeft: 40,
  },
  xLabel: {
    flex: 1,
    textAlign: 'center',
    fontSize: 10,
    color: '#9CA3AF',
  },
  xLabelSelected: {
    color: '#0055B2',
    fontWeight: '700',
  },
});
