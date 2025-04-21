import { apiService } from "@/core/services/api";
import { PaginatedData } from "@/core/services/api/types/paginated-data";
import { PriceAlertFiltersDto } from "@/price-alerts/dtos/price-alert-filters-dto";
import { PriceAlertEntity } from "@/price-alerts/entities/price-alert-entity";
import { priceAlertsQueryKey } from "@/price-alerts/hooks/query-options/price-alerts-query-key";
import { queryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const existsActivePriceAlertForProduct = async (
  accessToken: string,
  productId: string,
) => {
  const params: PriceAlertFiltersDto = {
    isActive: true,
    productId,
  };
  const { data } = await apiService.get<PaginatedData<PriceAlertEntity>>(
    "/api/v1/price-alerts",
    {
      params,
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );

  return data.page.totalElements === 1;
};

export const getExistsActivePriceAlertForProductQueryOptions = (
  accessToken: string,
  userId: string,
  productId: string,
) =>
  queryOptions<boolean, AxiosError>({
    // eslint-disable-next-line @tanstack/query/exhaustive-deps
    queryKey: [
      ...priceAlertsQueryKey,
      "existsForProduct",
      userId,
      productId,
    ] as const,
    queryFn: () => existsActivePriceAlertForProduct(accessToken, productId),
    staleTime: 60 * 60 * 1000, // 60 minutes
  });
