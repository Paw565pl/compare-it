import { getExistsActivePriceAlertForProductQueryOptions } from "@/price-alerts/hooks/query-options/get-exists-active-price-alert-for-product";
import { useQuery } from "@tanstack/react-query";

export const useExistsActivePriceAlertForProduct = (
  accessToken: string,
  userId: string,
  productId: string,
) =>
  useQuery(
    getExistsActivePriceAlertForProductQueryOptions(
      accessToken,
      userId,
      productId,
    ),
  );
