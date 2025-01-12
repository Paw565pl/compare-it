import { shopsListQueryOptions } from "@/products/hooks/query-options/shops-list-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchShopsList = (queryClient: QueryClient) =>
  queryClient.prefetchQuery(shopsListQueryOptions);
