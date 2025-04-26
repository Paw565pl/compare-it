import { getQueryClient } from "@/core/libs/tanstack-query";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { FavoriteProductDto } from "@/favorite-products/dto/favorite-product-dto";
import { favoriteProductsQueryKey } from "@/favorite-products/hooks/query-options/favorite-products-query-key";
import { useMutation } from "@tanstack/react-query";
import { AxiosError } from "axios";

const deleteFavoriteProduct = async (
  accessToken: string,
  favoriteProductDto: FavoriteProductDto,
) => {
  const { data } = await apiService.delete<undefined>(
    "/api/v1/favorite-products",
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
      data: favoriteProductDto,
    },
  );
  return data;
};

export const useDeleteFavoriteProduct = (accessToken: string) =>
  useMutation<undefined, AxiosError<ErrorResponse>, FavoriteProductDto>({
    mutationKey: [...favoriteProductsQueryKey, "delete"] as const,
    mutationFn: (favoriteProductDto) =>
      deleteFavoriteProduct(accessToken, favoriteProductDto),
    onSettled: () => {
      const queryClient = getQueryClient();
      const queryKey = favoriteProductsQueryKey;

      queryClient.invalidateQueries({ queryKey });
    },
  });
