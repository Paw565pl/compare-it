import { getIsProductFavoriteQueryOptions } from "@/favorite-products/hooks/query-options/get-is-product-favorite-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchIsProductFavorite = (
  queryClient: QueryClient,
  accessToken: string,
  productId: string,
  userId: string,
) =>
  queryClient.prefetchQuery(
    getIsProductFavoriteQueryOptions(accessToken, productId, userId),
  );
