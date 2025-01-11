import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { PaginatedData } from "@/core/services/api/types/paginated-data";
import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { ProductFiltersDto } from "@/products/dtos/product-filters-dto";
import { ProductListEntity } from "@/products/entities/product-list-entity";
import { productsQueryKey } from "@/products/hooks/products-query-key";
import { infiniteQueryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchProductPage = async (
  pageParam: unknown,
  productFilters?: ProductFiltersDto,
  paginationOptions?: PaginationOptions,
) => {
  const { data } = await apiService.get<PaginatedData<ProductListEntity>>(
    "/v1/products",
    {
      params: {
        page: pageParam,
        ...productFilters,
        ...paginationOptions,
      },
    },
  );
  return data;
};

export const getProductPageQueryOptions = (
  productFilters?: ProductFiltersDto,
  paginationOptions?: PaginationOptions,
) =>
  infiniteQueryOptions<
    PaginatedData<ProductListEntity>,
    AxiosError<ErrorResponse>
  >({
    queryKey: [...productsQueryKey, productFilters, paginationOptions] as const,
    queryFn: ({ pageParam }) =>
      fetchProductPage(pageParam, productFilters, paginationOptions),
    getNextPageParam: ({ page: { number, totalPages } }) =>
      number + 1 < totalPages ? number + 1 : undefined,
    initialPageParam: 0,
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
