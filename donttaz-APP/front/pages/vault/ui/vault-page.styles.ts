import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  content: {
    flex: 1,
  },
  loadingContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emptyContainer: {
    flex: 1,
    paddingTop: 100,
    alignItems: 'center',
    justifyContent: 'flex-start',
    gap: 12,
  },
  emptyText: {
    fontSize: 15,
    color: '#9CA3AF',
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#111827',
  },
  emptyDesc: {
    fontSize: 14,
    color: '#6B7280',
    textAlign: 'center',
    lineHeight: 22,
  },
  registerButton: {
    marginTop: 8,
    paddingVertical: 12,
    paddingHorizontal: 32,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#D1D5DB',
  },
  registerButtonText: {
    fontSize: 15,
    fontWeight: '500',
    color: '#111827',
  },
});

export const wishlistStyles = StyleSheet.create({
  emptyIconWrapper: {
    width: 88,
    height: 88,
    borderRadius: 44,
    backgroundColor: '#F3F4F6',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  progressSection: {
    width: '100%',
    paddingHorizontal: 24,
    paddingTop: 24,
    paddingBottom: 24,
    marginBottom: 8,
    backgroundColor: '#F9F9F9',
    overflow: 'visible',
  },
  fishImage: {
    position: 'absolute',
    top: -16,
    right: 25,
    width: 110,
    height: 130,
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  wishName: {
    fontSize: 26,
    fontWeight: '700',
    color: '#000000',
    marginTop: 65,
    marginBottom: 6,
  },
  amountLine: {
    fontSize: 18,
    color: '#545454',
    marginBottom: 16,
  },
  progressTrack: {
    height: 28,
    backgroundColor: '#E5E7EB',
    borderRadius: 8,
    overflow: 'hidden',
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#868686',
    borderRadius: 8,
  },
  progressLabels: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 6,
    marginBottom: 4,
  },
  progressLabelCurrent: {
    fontSize: 13,
    fontWeight: '600',
    color: '#9CA3AF',
  },
  progressLabelGoal: {
    fontSize: 13,
    color: '#9CA3AF',
  },
  buttonRow: {
    flexDirection: 'row',
    gap: 10,
    justifyContent: 'center',
    marginTop: 16,
  },
  button: {
    paddingVertical: 9,
    paddingHorizontal: 24,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#D1D5DB',
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
  },
  buttonPrimary: {},
  buttonOutline: {},
  buttonTextPrimary: {
    fontSize: 14,
    fontWeight: '500',
    color: '#111827',
  },
  buttonTextOutline: {
    fontSize: 14,
    fontWeight: '500',
    color: '#111827',
  },
  terminateButton: {
    marginHorizontal: 24,
    marginVertical: 8,
    paddingVertical: 14,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#FCA5A5',
    alignItems: 'center',
  },
  terminateButtonText: {
    fontSize: 14,
    fontWeight: '500',
    color: '#EF4444',
  },
});
