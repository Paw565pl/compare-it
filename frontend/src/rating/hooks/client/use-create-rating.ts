import { commentsQueryKey } from "@/comments/hooks/query-options/comments-query-key";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { productsQueryKey } from "@/products/hooks/query-options/products-query-key";
import { RatingDto } from "@/rating/dto/rating-dto";
import { RatingEntity } from "@/rating/entities/rating-entity";
import { useMutation } from "@tanstack/react-query";
import { AxiosError } from "axios";

const createRating = async (
  accessToken: string,
  productId: string,
  commentId: string,
  ratingDto: RatingDto,
) => {
  const { data } = await apiService.post<RatingEntity>(
    `/v1/products/${productId}/comments/${commentId}/rate`,
    ratingDto,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const useCreateRating = (
  accessToken: string,
  productId: string,
  commentId: string,
) =>
  useMutation<RatingEntity, AxiosError<ErrorResponse>, RatingDto>({
    mutationKey: [
      ...productsQueryKey,
      productId,
      ...commentsQueryKey,
      commentId,
      "rating",
      "create",
    ] as const,
    mutationFn: (ratingDto) =>
      createRating(accessToken, productId, commentId, ratingDto),
    onSuccess: () => {
      const queryClient = getQueryClient();
      const queryKey = [
        ...productsQueryKey,
        productId,
        ...commentsQueryKey,
      ] as const;

      queryClient.invalidateQueries({ queryKey });
    },
  });
