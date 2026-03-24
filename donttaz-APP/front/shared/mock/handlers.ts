// import paymentHistory from './data/payment-history.json';
import transfer from './data/transfer.json';
import monthlyReport from './data/monthly-report.json';
import yearlyReport from './data/yearly-report.json';
// import accounts from './data/accounts.json';
// import burndown from './data/burndown.json';
import burndown from './data/burndown-surviving.json';
import vaultTransactions from './data/vault-transactions.json';
import vaultWishlist from './data/vault-wishlist.json';

type MockHandler = {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  url: string;
  response: unknown;
};

export const mockHandlers: MockHandler[] = [
  // { method: 'GET', url: '/accounts', response: accounts },
  // { method: 'GET', url: '/accounts/:accountId/transactions', response: paymentHistory },
  {
    method: 'POST',
    url: '/accounts/transfer',
    response: transfer,
  },
  {
    method: 'GET',
    url: '/ai/transactions/categories',
    response: monthlyReport,
  },
  {
    method: 'GET',
    url: '/ai/transactions/yearly',
    response: yearlyReport,
  },
  {
    method: 'GET',
    url: '/control/burndown',
    response: burndown,
  },
  {
    method: 'GET',
    url: '/vaults/:vaultId/history',
    response: vaultTransactions,
  },
  {
    method: 'GET',
    url: '/vaults/:vaultId/wishlist',
    response: vaultWishlist,
  },
  {
    method: 'POST',
    url: '/vaults/:vaultId/wishlist',
    response: {},
  },
  {
    method: 'PATCH',
    url: '/vaults/:vaultId/wishlist/:wishId',
    response: {},
  },
];
