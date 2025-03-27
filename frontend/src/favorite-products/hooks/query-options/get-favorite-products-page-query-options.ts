import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { PaginatedData } from "@/core/services/api/types/paginated-data";
import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { favoriteProductsQueryKey } from "@/favorite-products/hooks/query-options/favorite-products-query-key";
import { ProductListEntity } from "@/products/entities/product-list-entity";
import { infiniteQueryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchFavoriteProductsPage = async (
  accessToken: string,
  pageParam: unknown,
  paginationOptions?: PaginationOptions,
) => {
  const { data } = await apiService.get<PaginatedData<ProductListEntity>>(
    "/api/v1/favorite-products",
    {
      params: {
        ...paginationOptions,
        page: pageParam,
      },
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const getFavoriteProductsPageQueryOptions = (
  accessToken: string,
  userId: string,
  paginationOptions?: PaginationOptions,
) =>
  infiniteQueryOptions<
    PaginatedData<ProductListEntity>,
    AxiosError<ErrorResponse>
  >({
    // eslint-disable-next-line @tanstack/query/exhaustive-deps
    queryKey: [...favoriteProductsQueryKey, userId, paginationOptions] as const,
    queryFn: ({ pageParam }) =>
      fetchFavoriteProductsPage(accessToken, pageParam, paginationOptions),
    getNextPageParam: ({ page: { number, totalPages } }) =>
      number + 1 < totalPages ? number + 1 : undefined,
    initialPageParam: 0,
    staleTime: 60 * 60 * 1000, // 60 minutes
  });
