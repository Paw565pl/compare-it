import { getExistsActivePriceAlertForProductQueryOptions } from "@/price-alerts/hooks/query-options/get-exists-active-price-alert-for-product";
import { QueryClient } from "@tanstack/react-query";

export const prefetchExistsActivePriceAlertForProduct = (
  queryClient: QueryClient,
  accessToken: string,
  userId: string,
  productId: string,
) =>
  queryClient.prefetchQuery(
    getExistsActivePriceAlertForProductQueryOptions(
      accessToken,
      userId,
      productId,
    ),
  );
