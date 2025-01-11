import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { PaginatedData } from "@/core/services/api/types/paginated-data";
import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { ProductListEntity } from "@/products/entities/product-list-entity";
import { productsQueryKey } from "@/products/hooks/products-query-key";
import { infiniteQueryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchProductPage = async (
  pageParam: unknown,
  paginationOptions?: PaginationOptions,
) => {
  const { data } = await apiService.get<PaginatedData<ProductListEntity>>(
    "/v1/products",
    {
      params: {
        page: pageParam,
        ...paginationOptions,
      },
    },
  );
  return data;
};

export const getProductPageQueryOptions = (
  paginationOptions?: PaginationOptions,
) =>
  infiniteQueryOptions<
    PaginatedData<ProductListEntity>,
    AxiosError<ErrorResponse>
  >({
    queryKey: [...productsQueryKey, paginationOptions] as const,
    queryFn: ({ pageParam }) => fetchProductPage(pageParam, paginationOptions),
    getNextPageParam: ({ page: { number, totalPages } }) =>
      number + 1 < totalPages ? number + 1 : undefined,
    initialPageParam: 0,
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
