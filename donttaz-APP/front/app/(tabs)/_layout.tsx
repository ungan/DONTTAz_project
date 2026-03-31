import { Redirect, Tabs } from 'expo-router';
import { useAppEntryCheck, useAuthCheck, useAuthRuntimeStore } from '@/features/auth';
import { BottomNavBar } from '@/widgets/nav-bar';

export default function TabLayout() {
  const authStatus = useAuthCheck();
  const appEntryStatus = useAppEntryCheck();
  const isPinVerified = useAuthRuntimeStore(state => state.isPinVerified);

  if (authStatus === 'loading' || appEntryStatus === 'loading') {
    return null;
  }

  if (authStatus === 'unauthenticated') {
    return <Redirect href="/login" />;
  }

  if (appEntryStatus === 'pin-required' && !isPinVerified) {
    return <Redirect href="/pin-login" />;
  }

  return (
    <Tabs
      screenOptions={{ headerShown: false }}
      tabBar={(props) => <BottomNavBar {...props} />}
    >
      <Tabs.Screen name="home" />
      <Tabs.Screen name="plan" />
      <Tabs.Screen name="vault" />
      <Tabs.Screen name="menu" />
    </Tabs>
  );
}
