import { apiClient } from '@/shared/api';
import type {
  AccountOpenApiResponse,
  DemandDepositProductListData,
} from '../types/account-open-api';

export async function getDemandDepositProductsApi() {
  const response = await apiClient.get<AccountOpenApiResponse<DemandDepositProductListData>>(
    '/accounts/demand-deposits/products'
  );

  return response.data;
}
