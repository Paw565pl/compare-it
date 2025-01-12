import { categoriesListQueryOptions } from "@/products/hooks/categories-list-query-options";
import { useQuery } from "@tanstack/react-query";

export const useFetchCategoriesList = () =>
  useQuery(categoriesListQueryOptions);
