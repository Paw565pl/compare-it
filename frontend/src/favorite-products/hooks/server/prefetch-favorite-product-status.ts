import { getFavoriteProductStatusQueryOptions } from "@/favorite-products/hooks/query-options/get-favorite-product-status-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchFavoriteProductStatus = (
  queryClient: QueryClient,
  accessToken: string,
  productId: string,
  userId: string,
) =>
  queryClient.prefetchQuery(
    getFavoriteProductStatusQueryOptions(accessToken, productId, userId),
  );
