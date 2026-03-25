export type VaultKind = 'DEFAULT' | 'WISHLIST';
export type VaultStatus = 'LOCKED' | 'MATURED' | 'BROKEN';
export type VaultTransactionType = 'DEPOSIT' | 'WITHDRAW_BROKEN';

export interface Vault {
  vaultId: number;
  vaultKind: VaultKind;
  lockedAmount: number;
  status: VaultStatus;
  wishName: string;
  goalAmount: number;
  baseInterestRate: number;
  penaltyInterestRate: number;
  withdrawalCount: number;
}

export interface GetVaultsResponse {
  vaults: Vault[];
}

export interface VaultTransaction {
  id: number;
  vaultId: number;
  transactionType: VaultTransactionType;
  amount: number;
  interestPaid: number;
  createdAt: string;
}

export interface GetVaultTransactionsResponse {
  status: string;
  message: string;
  data: VaultTransaction[];
}

export interface Wishlist {
  id: number;
  wishName: string;
  goalAmount: number;
  targetDate: string;
}

export interface WishlistRequest {
  wishName: string;
  goalAmount: number;
}

export interface GetWishlistResponse {
  data: Wishlist[];
}

export type VaultMemorandumStatus = 'WRITING' | 'SIGNED' | 'EXPOSED_WITHDRAW' | 'EXPOSED_TERMINATE';

export interface VaultMemorandum {
  id: number;
  wishlistVaultId: number;
  content1: string;
  content2: string;
  content3: string;
  status: VaultMemorandumStatus;
  signedAt: string | null;
}

export interface MemorandumRequest {
  content1: string;
  content2: string;
  content3: string;
}

export interface GetMemorandumResponse {
  data: VaultMemorandum;
}

export interface CreateWishlistVaultRequest {
  wishName: string;
  goalAmount: number;
}

export interface CreateWishlistVaultData {
  vaultId: number;
  vaultKind: VaultKind;
  lockedAmount: number;
  status: VaultStatus;
}

export interface CreateWishlistVaultResponse {
  status: string;
  message: string;
  data: CreateWishlistVaultData;
}

export interface DepositVaultRequest {
  amount: number;
}

export interface WithdrawVaultRequest {
  accountNo: string;
  amount: number;
}

export interface WithdrawVaultResponse {
  vaultId: number;
  withdrawnAmount: number;
  interestPaid: number;
  totalRefundAmount: number;
  withdrawalCount: number;
}

export interface DepositVaultResponse {
  vaultId: number;
  depositedAmount: number;
  lockedAmount: number;
}

export type PlatformType = 'MATTERMOST' | 'DISCORD';

export interface PenaltyWebhookRequest {
  platformType: PlatformType;
  shameMessage: string;
}

export interface PenaltyWebhookResponse {
  webhookId: number;
  platformType: PlatformType;
  shameMessage: string;
}