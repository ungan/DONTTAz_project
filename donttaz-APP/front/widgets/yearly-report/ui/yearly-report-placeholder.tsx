import React from 'react';
import { View, Text } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Primary } from '@/shared/constants/theme';
import { styles } from './yearly-report-placeholder.styles';

export function YearlyReportPlaceholder() {
  return (
    <View style={styles.container}>
      <View style={styles.iconWrap}>
        <Ionicons name="bar-chart-outline" size={28} color={Primary} />
      </View>
      <Text style={styles.title}>
        {'\uC5F0\uAC04 \uC18C\uBE44 \uB9AC\uD3EC\uD2B8\uB294 \uC900\uBE44 \uC911\uC785\uB2C8\uB2E4.'}
      </Text>
      <Text style={styles.description}>
        {
          '\uBA3C\uC800 \uC6D4\uAC04 \uB9AC\uD3EC\uD2B8\uC5D0\uC11C \uC774\uBC88 \uB2EC \uC18C\uBE44 \uD750\uB984\uACFC \uCE74\uD14C\uACE0\uB9AC\uBCC4 \uC9C0\uCD9C\uC744 \uD655\uC778\uD574\uBCF4\uC138\uC694.'
        }
      </Text>
    </View>
  );
}
