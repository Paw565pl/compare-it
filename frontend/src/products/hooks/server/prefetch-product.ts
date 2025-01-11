import { getProductQueryOptions } from "@/products/hooks/get-product-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchProduct = (queryClient: QueryClient, id: string) =>
  queryClient.prefetchQuery(getProductQueryOptions(id));
