import { getActivePriceAlertForProductQueryOptions } from "@/price-alerts/hooks/query-options/get-active-price-alert-for-product-query-options";
import { useQuery } from "@tanstack/react-query";

export const useFetchActivePriceAlertForProduct = (
  accessToken: string,
  userId: string,
  productId: string,
) =>
  useQuery(
    getActivePriceAlertForProductQueryOptions(accessToken, userId, productId),
  );
