import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { PriceAlertFiltersDto } from "@/price-alerts/dtos/price-alert-filters-dto";
import { getPriceAlertsPageQueryOptions } from "@/price-alerts/hooks/query-options/get-price-alerts-page-query-options";
import { useInfiniteQuery } from "@tanstack/react-query";

export const useFetchPriceAlertsPage = (
  accessToken: string,
  userId: string,
  priceAlertFiltersDto?: PriceAlertFiltersDto,
  paginationOptions?: PaginationOptions,
) =>
  useInfiniteQuery(
    getPriceAlertsPageQueryOptions(
      accessToken,
      userId,
      priceAlertFiltersDto,
      paginationOptions,
    ),
  );
