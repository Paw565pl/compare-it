import { getFavoriteProductStatusQueryOptions } from "@/favorite-products/hooks/query-options/get-favorite-product-status-query-options";
import { useQuery } from "@tanstack/react-query";

export const useFavoriteProductStatus = (
  accessToken: string,
  productId: string,
  userId: string,
) =>
  useQuery(
    getFavoriteProductStatusQueryOptions(accessToken, productId, userId),
  );
