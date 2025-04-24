import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { FavoriteProductStatusEntity } from "@/favorite-products/entities/favorite-product-status-entity";
import { favoriteProductsQueryKey } from "@/favorite-products/hooks/query-options/favorite-products-query-key";
import { queryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchFavoriteProductStatus = async (
  accessToken: string,
  productId: string,
) => {
  const { data } = await apiService.get<FavoriteProductStatusEntity>(
    `/api/v1/favorite-products/${productId}/status`,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const getFavoriteProductStatusQueryOptions = (
  accessToken: string,
  productId: string,
  userId: string,
) =>
  queryOptions<FavoriteProductStatusEntity, AxiosError<ErrorResponse>>({
    // eslint-disable-next-line @tanstack/query/exhaustive-deps
    queryKey: [
      ...favoriteProductsQueryKey,
      "status",
      productId,
      userId,
    ] as const,
    queryFn: () => fetchFavoriteProductStatus(accessToken, productId),
    staleTime: 60 * 60 * 1000, // 60 minutes
    enabled: !!accessToken && !!userId,
  });
