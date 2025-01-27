import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { getFavoriteProductsPageQueryOptions } from "@/favorite-products/hooks/query-options/get-favorite-products-page-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchFavoriteProductsPage = (
  queryClient: QueryClient,
  accessToken: string,
  userId: string,
  paginationOptions?: PaginationOptions,
) =>
  queryClient.prefetchInfiniteQuery(
    getFavoriteProductsPageQueryOptions(accessToken, userId, paginationOptions),
  );
