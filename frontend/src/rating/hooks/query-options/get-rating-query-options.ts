import { commentsQueryKey } from "@/comments/hooks/query-options/comments-query-key";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { productsQueryKey } from "@/products/hooks/query-options/products-query-key";
import { RatingEntity } from "@/rating/entities/rating-entity";
import { queryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchRating = async (
  accessToken: string,
  productId: string,
  commentId: string,
) => {
  const { data } = await apiService.get<RatingEntity>(
    `/v1/products/${productId}/comments/${commentId}/rate`,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const getRatingQueryOptions = (
  accessToken: string,
  userId: string,
  productId: string,
  commentId: string,
) =>
  queryOptions<RatingEntity, AxiosError<ErrorResponse>>({
    // eslint-disable-next-line @tanstack/query/exhaustive-deps
    queryKey: [
      ...productsQueryKey,
      productId,
      ...commentsQueryKey,
      commentId,
      "rating",
      userId,
    ] as const,
    queryFn: () => fetchRating(accessToken, productId, commentId),
    staleTime: 60 * 60 * 1000, // 60 minutes
  });
