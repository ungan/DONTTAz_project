import React, { useState } from 'react';
import { View, Text, TouchableOpacity, Modal, FlatList } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { styles } from './select-input.styles';

interface SelectInputProps {
  label?: string;
  options: string[];
  value: string;
  placeholder?: string;
  onChange: (value: string) => void;
  getOptionLabel?: (value: string) => string;
}

export function SelectInput({
  label,
  options,
  value,
  placeholder = '선택',
  onChange,
  getOptionLabel,
}: SelectInputProps) {
  const [visible, setVisible] = useState(false);
  const toOptionLabel = getOptionLabel ?? ((option: string) => option);

  const handleSelect = (option: string) => {
    onChange(option);
    setVisible(false);
  };

  return (
    <View style={styles.wrapper}>
      {label && <Text style={styles.label}>{label}</Text>}

      <TouchableOpacity
        style={styles.selector}
        onPress={() => setVisible(true)}
        activeOpacity={0.7}
      >
        <Text style={[styles.selectorText, !value && styles.placeholder]}>
          {value ? toOptionLabel(value) : placeholder}
        </Text>
        <Ionicons name="chevron-down" size={20} color="#6B7280" />
      </TouchableOpacity>

      <Modal visible={visible} transparent animationType="fade">
        <TouchableOpacity
          style={styles.overlay}
          activeOpacity={1}
          onPress={() => setVisible(false)}
        >
          <View style={styles.modal}>
            <FlatList
              data={options}
              keyExtractor={(item) => item}
              renderItem={({ item }) => (
                <TouchableOpacity
                  style={[styles.option, item === value && styles.optionSelected]}
                  onPress={() => handleSelect(item)}
                  activeOpacity={0.7}
                >
                  <Text style={[styles.optionText, item === value && styles.optionTextSelected]}>
                    {toOptionLabel(item)}
                  </Text>
                </TouchableOpacity>
              )}
            />
          </View>
        </TouchableOpacity>
      </Modal>
    </View>
  );
}
