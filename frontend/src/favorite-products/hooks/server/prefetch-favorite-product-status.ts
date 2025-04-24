import { getFavoriteProductStatusQueryOptions } from "@/favorite-products/hooks/query-options/get-favorite-product-status-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchFavoriteProductStatus = (
  queryClient: QueryClient,
  accessToken: string,
  userId: string,
  productId: string,
) =>
  queryClient.prefetchQuery(
    getFavoriteProductStatusQueryOptions(accessToken, userId, productId),
  );
