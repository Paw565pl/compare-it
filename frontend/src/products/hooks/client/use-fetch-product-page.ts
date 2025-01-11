import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { getProductPageQueryOptions } from "@/products/hooks/get-product-page-query-options";
import { useInfiniteQuery } from "@tanstack/react-query";

export const useFetchProductPage = (paginationOptions?: PaginationOptions) =>
  useInfiniteQuery(getProductPageQueryOptions(paginationOptions));
