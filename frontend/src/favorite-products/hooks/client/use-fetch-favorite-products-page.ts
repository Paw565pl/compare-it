import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { getFavoriteProductsPageQueryOptions } from "@/favorite-products/hooks/query-options/get-favorite-products-page-query-options";
import { useInfiniteQuery } from "@tanstack/react-query";

export const useFetchFavoriteProductsPage = (
  accessToken: string,
  userId: string,
  paginationOptions?: PaginationOptions,
) =>
  useInfiniteQuery(
    getFavoriteProductsPageQueryOptions(accessToken, userId, paginationOptions),
  );
