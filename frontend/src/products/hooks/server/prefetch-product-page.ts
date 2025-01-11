import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { ProductFiltersDto } from "@/products/dtos/product-filters-dto";
import { getProductPageQueryOptions } from "@/products/hooks/get-product-page-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchProductPage = (
  queryClient: QueryClient,
  productFilters?: ProductFiltersDto,
  paginationOptions?: PaginationOptions,
) =>
  queryClient.prefetchInfiniteQuery(
    getProductPageQueryOptions(productFilters, paginationOptions),
  );
