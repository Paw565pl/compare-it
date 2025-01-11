import { getProductPageQueryOptions } from "@/products/hooks/get-product-page-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchProductPage = (queryClient: QueryClient) =>
  queryClient.prefetchInfiniteQuery(getProductPageQueryOptions());
