import React, { useEffect } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { KeyboardAvoidingView, Platform, ScrollView, Text, View } from 'react-native';
import { useRouter } from 'expo-router';
import { useMyAccounts } from '@/entities/account';
import { useBankAccountStore } from '@/entities/bank-account';
import { PageHeader, Button } from '@/shared/ui';
import {
  TransferFormSection,
  TransferSourceAccountSection,
  useTransferForm,
  useTransferStore,
} from '@/features/transfer';
import type { AccountSummary } from '@/entities/account';
import { styles } from './transfer-account-page.styles';

function sanitizeAccountNumber(accountNumber: string | null | undefined) {
  return accountNumber?.replace(/[^0-9]/g, '') ?? '';
}

function maskAccountNumber(accountNumber: string) {
  if (accountNumber.length < 9) {
    return accountNumber;
  }

  return `${accountNumber.slice(0, 3)}-***-${accountNumber.slice(-6)}`;
}

function parseBalance(balance: string | number | null | undefined) {
  const numericBalance = typeof balance === 'number' ? balance : Number(balance ?? 0);
  return Number.isFinite(numericBalance) ? numericBalance : 0;
}

function resolveTransferableAccountNo(account: AccountSummary, storedAccountNo: string | null) {
  const rawAccountNumber = sanitizeAccountNumber(account.accountNumber);

  if (/^[0-9]{10,20}$/.test(rawAccountNumber)) {
    return rawAccountNumber;
  }

  const rawAccountNo = sanitizeAccountNumber(account.accountNo);

  if (/^[0-9]{10,20}$/.test(rawAccountNo) && !account.accountNo.includes('*')) {
    return rawAccountNo;
  }

  const sanitizedStoredAccountNo = sanitizeAccountNumber(storedAccountNo);

  if (
    sanitizedStoredAccountNo &&
    account.accountNo === maskAccountNumber(sanitizedStoredAccountNo)
  ) {
    return sanitizedStoredAccountNo;
  }

  return '';
}

export function TransferAccountPage() {
  const router = useRouter();
  const { form, setBank, setAccountNumber, isValid } = useTransferForm();
  const {
    setAccountInfo,
    setWithdrawalAccount,
    withdrawalAccountNo,
    withdrawalAccountLabel,
    withdrawalBankName,
    withdrawalAccountName,
  } = useTransferStore();
  const {
    accountNo: storedAccountNo,
    accountId: storedAccountId,
    bankName: storedBankName,
    balance: storedBalance,
    accountName: storedAccountName,
  } = useBankAccountStore();
  const { accounts, isPending, error } = useMyAccounts();

  const accountOptions = accounts.map((account) => {
    const transferableAccountNo = resolveTransferableAccountNo(account, storedAccountNo);
    const accountId = account.accountId ?? `${account.bankCode}-${account.accountNo}`;

    return {
      id: String(accountId),
      bankName: account.bankName,
      accountName: account.accountName,
      accountLabel: account.accountNo,
      balanceLabel: `${parseBalance(account.accountBalance).toLocaleString('ko-KR')}원`,
      isPrimary: account.isPrimary,
      isSelectable: !!transferableAccountNo,
      transferableAccountNo,
    };
  });

  if (
    accountOptions.length === 0 &&
    storedAccountNo &&
    storedBankName &&
    storedAccountName &&
    storedBalance !== null
  ) {
    accountOptions.push({
      id: String(storedAccountId ?? storedAccountNo),
      bankName: storedBankName,
      accountName: storedAccountName,
      accountLabel: maskAccountNumber(storedAccountNo),
      balanceLabel: `${storedBalance.toLocaleString('ko-KR')}원`,
      isPrimary: true,
      isSelectable: true,
      transferableAccountNo: sanitizeAccountNumber(storedAccountNo),
    });
  }

  useEffect(() => {
    const hasSelectedAccount = accountOptions.some(
      (account) =>
        account.transferableAccountNo === withdrawalAccountNo &&
        account.accountLabel === withdrawalAccountLabel
    );

    if (hasSelectedAccount) {
      return;
    }

    const defaultAccount =
      accountOptions.find((account) => account.isPrimary && account.isSelectable) ??
      accountOptions.find((account) => account.isSelectable);

    if (!defaultAccount) {
      return;
    }

    setWithdrawalAccount(
      defaultAccount.bankName,
      defaultAccount.accountName,
      defaultAccount.accountLabel,
      defaultAccount.transferableAccountNo
    );
  }, [
    accountOptions,
    setWithdrawalAccount,
    withdrawalAccountLabel,
    withdrawalAccountNo,
  ]);

  const selectedAccountId =
    accountOptions.find(
      (account) =>
        account.transferableAccountNo === withdrawalAccountNo &&
        account.accountLabel === withdrawalAccountLabel
    )?.id ?? null;

  const handleSelectWithdrawalAccount = (accountId: string) => {
    const selectedAccount = accountOptions.find((account) => account.id === accountId);

    if (!selectedAccount || !selectedAccount.isSelectable) {
      return;
    }

    setWithdrawalAccount(
      selectedAccount.bankName,
      selectedAccount.accountName,
      selectedAccount.accountLabel,
      selectedAccount.transferableAccountNo
    );
  };

  const handleConfirm = () => {
    setAccountInfo(form.bankName, form.accountNumber);
    router.push('/transfer-amount');
  };

  const isReadyToProceed = isValid && !!withdrawalAccountNo;

  return (
    <SafeAreaView style={styles.container}>
      <PageHeader title="이체" />

      <KeyboardAvoidingView
        style={styles.keyboardAvoid}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      >
        <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
          <Text style={styles.question}>어느 계좌에서 보내고 어디로 입금할까요?</Text>

          <TransferSourceAccountSection
            accounts={accountOptions}
            selectedAccountId={selectedAccountId}
            isPending={isPending}
            error={error}
            onSelect={handleSelectWithdrawalAccount}
          />

          {withdrawalAccountNo ? (
            <View style={styles.selectedAccountBox}>
              <Text style={styles.selectedAccountLabel}>선택된 출금 계좌</Text>
              <Text
                style={styles.selectedAccountValue}
              >{`${withdrawalBankName} ${withdrawalAccountName}`}</Text>
              <Text style={styles.selectedAccountSubValue}>{withdrawalAccountLabel}</Text>
            </View>
          ) : null}

          <View style={styles.formAndButton}>
            <TransferFormSection
              form={form}
              onBankChange={setBank}
              onAccountNumberChange={setAccountNumber}
            />
            <Button label="다음" disabled={!isReadyToProceed} onPress={handleConfirm} />
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
