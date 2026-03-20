import { SafeAreaView } from 'react-native-safe-area-context';
import { PageHeader } from '@/shared/ui';
import { styles } from './vault-page.styles';

export function VaultPage() {
  return (
    <SafeAreaView style={styles.container}>
      <PageHeader title="가상금고" />
    </SafeAreaView>
  );
}
