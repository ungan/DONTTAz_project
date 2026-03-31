import { StyleSheet } from 'react-native';
export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  content: {
    paddingHorizontal: 14,
    paddingTop: 12,
    paddingBottom: 28,
  },
  tabRow: {
    flexDirection: 'row',
    marginHorizontal: 4,
    marginBottom: 18,
    padding: 4,
    borderRadius: 16,
    backgroundColor: '#F3F4F6',
    overflow: 'hidden',
  },
  tabButton: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 44,
    borderRadius: 12,
  },
  tabButtonActive: {
    backgroundColor: '#FFFFFF',
  },
  tabLabel: {
    fontSize: 15,
    fontWeight: '600',
    color: '#6B7280',
  },
  tabLabelActive: {
    color: '#111827',
  },
  firstSection: {
    marginTop: 0,
  },
  section: {
    marginTop: 16,
  },
  helperText: {
    marginTop: 10,
    fontSize: 14,
    lineHeight: 20,
    color: '#6B7280',
    textAlign: 'center',
  },
  loaderWrap: {
    paddingVertical: 48,
    alignItems: 'center',
  },
  loader: {
    marginTop: 16,
  },
  previewSection: {
    position: 'relative',
  },
  previewChartWrap: {
    opacity: 0.28,
  },
  previewOverlay: {
    position: 'absolute',
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 28,
  },
  previewMessageCard: {
    width: '100%',
    maxWidth: 320,
    borderRadius: 20,
    backgroundColor: 'rgba(255,255,255,0.94)',
    paddingVertical: 24,
    paddingHorizontal: 20,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 10,
    elevation: 3,
  },
  stateCard: {
    borderRadius: 20,
    backgroundColor: '#FFFFFF',
    paddingVertical: 28,
    paddingHorizontal: 20,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 8,
    elevation: 2,
  },
  stateTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
    textAlign: 'center',
  },
  stateDescription: {
    marginTop: 10,
    fontSize: 14,
    lineHeight: 21,
    color: '#6B7280',
    textAlign: 'center',
  },
  stateButtonWrap: {
    marginTop: 20,
    width: '100%',
  },
  dashboardHeading: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
    paddingLeft: 16,
  },
  dashboardDescription: {
    marginTop: 6,
    fontSize: 14,
    lineHeight: 20,
    color: '#6B7280',
  },
  ctaWrap: {
    marginTop: 18,
  },
});
