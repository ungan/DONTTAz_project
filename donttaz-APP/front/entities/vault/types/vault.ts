export type VaultKind = 'DEFAULT';
export type VaultStatus = 'LOCKED' | 'UNLOCKED';
export type VaultTransactionType = 'DEPOSIT' | 'WITHDRAWAL';

export interface Vault {
  vaultId: number;
  vaultKind: VaultKind;
  lockedAmount: number;
  status: VaultStatus;
  wishName: string;
  goalAmount: number;
  targetDate: string;
  baseInterestRate: number;
  penaltyInterestRate: number;
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
  targetDate: string;
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

export type WebhookPlatform = 'MATTERMOST' | 'KAKAO_TALK';

export interface WebhookConfig {
  platform: WebhookPlatform;
  message: string;
}