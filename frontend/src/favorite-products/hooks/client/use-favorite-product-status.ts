import { getFavoriteProductStatusQueryOptions } from "@/favorite-products/hooks/query-options/get-favorite-product-status-query-options";
import { useQuery } from "@tanstack/react-query";

export const useFavoriteProductStatus = (
  accessToken: string,
  userId: string,
  productId: string,
) =>
  useQuery(
    getFavoriteProductStatusQueryOptions(accessToken, userId, productId),
  );
