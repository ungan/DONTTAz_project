import { Tabs } from 'expo-router';

import { BottomNavBar } from '@/widgets/nav-bar';

export default function TabLayout() {
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
