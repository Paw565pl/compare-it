/* eslint-disable @typescript-eslint/no-invalid-void-type */
import { getQueryClient } from "@/core/libs/tanstack-query";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { FavoriteProductDto } from "@/favorite-products/dto/favorite-product-dto";
import { favoriteProductsQueryKey } from "@/favorite-products/hooks/query-options/favorite-products-query-key";
import { useMutation } from "@tanstack/react-query";
import { AxiosError } from "axios";

const addFavoriteProduct = async (
  accessToken: string,
  favoriteProductDto: FavoriteProductDto,
) => {
  const { data } = await apiService.put<void>(
    "/v1/favorite-products",
    favoriteProductDto,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const useAddFavoriteProduct = (accessToken: string) =>
  useMutation<void, AxiosError<ErrorResponse>, FavoriteProductDto>({
    mutationKey: [...favoriteProductsQueryKey, "add"] as const,
    mutationFn: (favoriteProductDto) =>
      addFavoriteProduct(accessToken, favoriteProductDto),
    onSuccess: () => {
      const queryClient = getQueryClient();
      const queryKey = favoriteProductsQueryKey;

      queryClient.invalidateQueries({ queryKey });
    },
  });
