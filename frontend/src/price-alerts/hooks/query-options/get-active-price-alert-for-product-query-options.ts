import { apiService } from "@/core/services/api";
import { PaginatedData } from "@/core/services/api/types/paginated-data";
import { PriceAlertFiltersDto } from "@/price-alerts/dtos/price-alert-filters-dto";
import { PriceAlertEntity } from "@/price-alerts/entities/price-alert-entity";
import { priceAlertsQueryKey } from "@/price-alerts/hooks/query-options/price-alerts-query-key";
import { queryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const getActivePriceAlertForProduct = async (
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

  return data.content.at(0);
};

export const getActivePriceAlertForProductQueryOptions = (
  accessToken: string,
  userId: string,
  productId: string,
) =>
  queryOptions<PriceAlertEntity | undefined, AxiosError>({
    // eslint-disable-next-line @tanstack/query/exhaustive-deps
    queryKey: [...priceAlertsQueryKey, productId, userId, "active"] as const,
    queryFn: () => getActivePriceAlertForProduct(accessToken, productId),
    staleTime: 60 * 60 * 1000, // 60 minutes,
    enabled: !!accessToken && !!userId,
  });
