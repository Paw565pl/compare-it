import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { CategoryEntity } from "@/products/entities/category-entity";
import { queryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchCategoriesList = async () => {
  const { data } = await apiService.get<CategoryEntity[]>("/api/v1/categories");
  return data;
};

export const categoriesListQueryOptions = queryOptions<
  CategoryEntity[],
  AxiosError<ErrorResponse>
>({
  queryKey: ["categories"] as const,
  queryFn: () => fetchCategoriesList(),
  staleTime: 1 * 60 * 60 * 1000, // 1 hour
});
