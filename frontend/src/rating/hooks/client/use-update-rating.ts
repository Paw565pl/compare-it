import { CommentEntity } from "@/comments/entities/comment-entity";
import { commentsQueryKey } from "@/comments/hooks/query-options/comments-query-key";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { PaginatedData } from "@/core/services/api/types/paginated-data";
import { productsQueryKey } from "@/products/hooks/query-options/products-query-key";
import { RatingDto } from "@/rating/dto/rating-dto";
import { RatingEntity } from "@/rating/entities/rating-entity";
import { InfiniteData, useMutation } from "@tanstack/react-query";
import { AxiosError } from "axios";

const updateRating = async (
  accessToken: string,
  productId: string,
  commentId: string,
  ratingDto: RatingDto,
) => {
  const { data } = await apiService.put<RatingEntity>(
    `/api/v1/products/${productId}/comments/${commentId}/rate`,
    ratingDto,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const useUpdateRating = (
  accessToken: string,
  productId: string,
  commentId: string,
) => {
  const queryClient = getQueryClient();
  const commentPageQueryKey = [
    ...productsQueryKey,
    productId,
    ...commentsQueryKey,
  ] as const;

  return useMutation<
    RatingEntity,
    AxiosError<ErrorResponse>,
    RatingDto,
    InfiniteData<PaginatedData<CommentEntity>>
  >({
    mutationKey: [
      ...productsQueryKey,
      productId,
      ...commentsQueryKey,
      commentId,
      "rating",
      "update",
    ] as const,
    mutationFn: (ratingDto) =>
      updateRating(accessToken, productId, commentId, ratingDto),
    onMutate: async (ratingDto) => {
      await queryClient.cancelQueries({ queryKey: commentPageQueryKey });

      const previousCommentInfiniteData = queryClient.getQueryData<
        InfiniteData<PaginatedData<CommentEntity>>
      >(commentPageQueryKey) ?? { pageParams: [], pages: [] };

      const newCommentPages = previousCommentInfiniteData?.pages.map(
        ({ page, content }) => ({
          page,
          content: content.map((comment) => {
            if (comment.id !== commentId) return comment;

            const newPositiveRatingsCount = ratingDto.isPositive
              ? comment.positiveRatingsCount + 1
              : comment.positiveRatingsCount - 1;
            const newNegativeRatingsCount = ratingDto.isPositive
              ? comment.negativeRatingsCount - 1
              : comment.negativeRatingsCount + 1;

            return {
              ...comment,
              positiveRatingsCount: newPositiveRatingsCount,
              negativeRatingsCount: newNegativeRatingsCount,
            };
          }),
        }),
      );
      const newCommentInfiniteData = {
        pageParams: previousCommentInfiniteData?.pageParams,
        pages: newCommentPages,
      };
      queryClient.setQueryData(commentPageQueryKey, newCommentInfiniteData);

      return previousCommentInfiniteData;
    },
    onError: (_, __, previousCommentPage) => {
      queryClient.setQueryData(commentPageQueryKey, previousCommentPage);
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: commentPageQueryKey });
    },
  });
};
