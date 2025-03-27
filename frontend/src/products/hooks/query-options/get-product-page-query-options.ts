import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { PaginatedData } from "@/core/services/api/types/paginated-data";
import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { ProductFiltersDto } from "@/products/dtos/product-filters-dto";
import { ProductListEntity } from "@/products/entities/product-list-entity";
import { productsQueryKey } from "@/products/hooks/query-options/products-query-key";
import { queryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchProductPage = async (
  productFilters?: ProductFiltersDto,
  paginationOptions?: PaginationOptions,
) => {
  const { data } = await apiService.get<PaginatedData<ProductListEntity>>(
    "/api/v1/products",
    {
      params: {
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
  queryOptions<PaginatedData<ProductListEntity>, AxiosError<ErrorResponse>>({
    queryKey: [...productsQueryKey, productFilters, paginationOptions] as const,
    queryFn: () => fetchProductPage(productFilters, paginationOptions),
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
