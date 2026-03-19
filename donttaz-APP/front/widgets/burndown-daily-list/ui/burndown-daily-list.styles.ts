import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 16,
  },
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 8,
    elevation: 2,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  cardTitle: {
    fontSize: 22,
    fontWeight: '900',
    color: '#111827',
  },
  toggleButton: {
    width: 36,
    height: 36,
    borderRadius: 10,
    backgroundColor: '#F3F4F6',
    alignItems: 'center',
    justifyContent: 'center',
  },
  tableHeader: {
    flexDirection: 'row',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#F3F4F6',
    marginBottom: 4,
  },
  tableHeaderText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#9CA3AF',
    textAlign: 'right',
  },
  row: {
    flexDirection: 'row',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#F9FAFB',
    alignItems: 'center',
  },
  cellDate: {
    flex: 0.8,
    fontSize: 15,
    color: '#374151',
    fontWeight: '600',
  },
  cellActual: {
    flex: 1.75,
    fontSize: 15,
    color: '#374151',
    textAlign: 'center',
  },
  cellActualEmpty: {
    flex: 1.75,
    fontSize: 15,
    color: '#D1D5DB',
    textAlign: 'center',
  },
  cellDiff: {
    flex: 1.75,
    fontSize: 15,
    fontWeight: '600',
    textAlign: 'right',
  },
});
