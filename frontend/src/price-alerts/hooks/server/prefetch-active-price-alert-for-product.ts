import { getActivePriceAlertForProductQueryOptions } from "@/price-alerts/hooks/query-options/get-active-price-alert-for-product-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchActivePriceAlertForProduct = (
  queryClient: QueryClient,
  accessToken: string,
  userId: string,
  productId: string,
) =>
  queryClient.prefetchQuery(
    getActivePriceAlertForProductQueryOptions(accessToken, userId, productId),
  );
