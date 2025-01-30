import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { PaginatedData } from "@/core/services/api/types/paginated-data";
import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { PriceAlertFiltersDto } from "@/price-alerts/dtos/price-alert-filters-dto";
import { PriceAlertEntity } from "@/price-alerts/entities/price-alert-entity";
import { priceAlertsQueryKey } from "@/price-alerts/hooks/query-options/price-alerts-query-key";
import { infiniteQueryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchPriceAlertsPage = async (
  accessToken: string,
  pageParam: unknown,
  priceAlertFiltersDto?: PriceAlertFiltersDto,
  paginationOptions?: PaginationOptions,
) => {
  const { data } = await apiService.get<PaginatedData<PriceAlertEntity>>(
    "/v1/price-alerts",
    {
      params: {
        page: pageParam,
        ...priceAlertFiltersDto,
        ...paginationOptions,
      },
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const getPriceAlertsPageQueryOptions = (
  accessToken: string,
  userId: string,
  priceAlertFiltersDto?: PriceAlertFiltersDto,
  paginationOptions?: PaginationOptions,
) =>
  infiniteQueryOptions<
    PaginatedData<PriceAlertEntity>,
    AxiosError<ErrorResponse>
  >({
    // eslint-disable-next-line @tanstack/query/exhaustive-deps
    queryKey: [
      ...priceAlertsQueryKey,
      userId,
      priceAlertFiltersDto,
      paginationOptions,
    ] as const,
    queryFn: ({ pageParam }) =>
      fetchPriceAlertsPage(
        accessToken,
        pageParam,
        priceAlertFiltersDto,
        paginationOptions,
      ),
    getNextPageParam: ({ page: { number, totalPages } }) =>
      number + 1 < totalPages ? number + 1 : undefined,
    initialPageParam: 0,
  });
