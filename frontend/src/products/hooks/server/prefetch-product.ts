import { ProductDetailsFiltersDto } from "@/products/dtos/product-details-filters-dto";
import { getProductQueryOptions } from "@/products/hooks/query-options/get-product-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchProduct = (
  queryClient: QueryClient,
  id: string,
  params?: ProductDetailsFiltersDto,
) => queryClient.prefetchQuery(getProductQueryOptions(id, params));
