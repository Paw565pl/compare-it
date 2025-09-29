import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { ShopEntity } from "@/products/entities/shop-entity";
import { queryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchShopsList = async () => {
  const { data } = await apiService.get<ShopEntity[]>("/api/v1/shops");
  return data;
};

export const shopsListQueryOptions = queryOptions<
  ShopEntity[],
  AxiosError<ErrorResponse>
>({
  queryKey: ["shops"] as const,
  queryFn: () => fetchShopsList(),
  staleTime: 1 * 60 * 60 * 1000, // 1 hour
});
