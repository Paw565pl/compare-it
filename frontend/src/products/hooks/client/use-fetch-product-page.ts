import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { ProductFiltersDto } from "@/products/dtos/product-filters-dto";
import { getProductPageQueryOptions } from "@/products/hooks/query-options/get-product-page-query-options";
import { useInfiniteQuery } from "@tanstack/react-query";

export const useFetchProductPage = (
  productFilters?: ProductFiltersDto,
  paginationOptions?: PaginationOptions,
) =>
  useInfiniteQuery(
    getProductPageQueryOptions(productFilters, paginationOptions),
  );
