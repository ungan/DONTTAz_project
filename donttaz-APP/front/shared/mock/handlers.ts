import paymentHistory from './data/payment-history.json';
import transfer from './data/transfer.json';
import monthlyReport from './data/monthly-report.json';
import yearlyReport from './data/yearly-report.json';
import accounts from './data/accounts.json';
// import burndown from './data/burndown.json';
import burndown from './data/burndown-surviving.json'

type MockHandler = {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  url: string;
  response: unknown;
};

export const mockHandlers: MockHandler[] = [
  {
    method: 'POST',
    url: '/auth/login',
    response: {
      status: 'SUCCESS',
      message: '로그인이 성공적으로 완료되었습니다.',
      data: {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
        userId: 105,
        name: '김토피',
      },
    },
  },
  {
    method: 'POST',
    url: '/auth/verify-identity',
    response: {
      status: 'SUCCESS',
      message: '본인인증이 완료되었습니다.',
      data: {
        name: '홍길동',
        phoneNumber: '01012345678',
        isRegistered: false,
        maskedEmail: null,
      },
    },
  },
  {
    method: 'POST',
    url: '/users/signup',
    response: {
      status: 'SUCCESS',
      message: '회원가입이 완료되었습니다.',
      data: {
        userId: 105,
        email: 'hong@example.com',
        name: '홍길동',
      },
    },
  },
  {
    method: 'GET',
    url: '/users/me',
    response: {
      status: 'SUCCESS',
      message: '사용자 정보 조회 성공',
      data: {
        userId: 105,
        email: 'hong@example.com',
        name: '홍길동',
        phoneNumber: '01012345678',
      },
    },
  },
  {
    method: 'GET',
    url: '/accounts/demand-deposits/products',
    response: {
      status: 'SUCCESS',
      message: '수시입출금 상품 목록 조회 성공',
      data: {
        products: [
          {
            accountTypeUniqueNo: 'mock-demand-deposit-001',
            bankCode: '092',
            bankName: 'DONTTAZ',
            accountTypeCode: '01',
            accountTypeName: '수시입출금',
            accountName: 'DONTTAZ 입출금통장',
            accountDescription: '생활비 관리를 위한 기본 입출금 통장',
            accountType: 'DEMAND_DEPOSIT',
          },
        ],
      },
    },
  },
  {
    method: 'POST',
    url: '/accounts',
    response: {
      status: 'SUCCESS',
      message: '계좌가 성공적으로 생성되었습니다.',
      data: {
        bankCode: '092',
        accountNo: '0921234567890123',
        currency: 'KRW',
        currencyName: '원화',
      },
    },
  },
  {
    method: 'GET',
    url: '/accounts',
    response: accounts,
  },
  {
    method: 'GET',
    url: '/accounts/:accountId/transactions',
    response: paymentHistory,
  },
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
];
