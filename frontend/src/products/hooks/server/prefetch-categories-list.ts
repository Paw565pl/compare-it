import { categoriesListQueryOptions } from "@/products/hooks/query-options/categories-list-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchCategoriesList = (queryClient: QueryClient) =>
  queryClient.prefetchQuery(categoriesListQueryOptions);
