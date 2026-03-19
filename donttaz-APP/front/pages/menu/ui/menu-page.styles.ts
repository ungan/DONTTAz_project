import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    paddingHorizontal: 16,
    paddingTop: 24,
    paddingBottom: 32,
    backgroundColor: '#FFFFFF',
    justifyContent: 'space-between',
  },
  content: {
    gap: 16,
  },
  heroCard: {
    padding: 20,
    borderRadius: 20,
    backgroundColor: Primary,
    gap: 8,
  },
  heroLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: '#DBEAFE',
  },
  heroTitle: {
    fontSize: 24,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  heroDescription: {
    fontSize: 14,
    lineHeight: 20,
    color: '#EFF6FF',
  },
  sectionCard: {
    padding: 20,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    backgroundColor: '#FFFFFF',
    gap: 10,
  },
  sectionLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: '#6B7280',
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
  },
  sectionText: {
    fontSize: 14,
    lineHeight: 20,
    color: '#4B5563',
  },
  errorText: {
    fontSize: 14,
    lineHeight: 20,
    color: '#DC2626',
  },
  bottomSection: {
    gap: 12,
  },
  helperText: {
    fontSize: 13,
    lineHeight: 19,
    color: '#6B7280',
    textAlign: 'center',
  },
});
