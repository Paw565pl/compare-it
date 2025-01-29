import { CommentDto } from "@/comments/dtos/comment-dto";
import { CommentEntity } from "@/comments/entities/comment-entity";
import { commentsQueryKey } from "@/comments/hooks/query-options/comments-query-key";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { productsQueryKey } from "@/products/hooks/query-options/products-query-key";
import { useMutation } from "@tanstack/react-query";
import { AxiosError } from "axios";

const updateComment = async (
  accessToken: string,
  productId: string,
  commentId: string,
  commentDto: CommentDto,
) => {
  const { data } = await apiService.put<CommentEntity>(
    `/v1/products/${productId}/comments/${commentId}`,
    commentDto,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const useUpdateComment = (
  accessToken: string,
  productId: string,
  commentId: string,
) =>
  useMutation<CommentEntity, AxiosError<ErrorResponse>, CommentDto>({
    mutationKey: [
      ...productsQueryKey,
      productId,
      ...commentsQueryKey,
      commentId,
      "update",
    ] as const,
    mutationFn: (commentDto) =>
      updateComment(accessToken, productId, commentId, commentDto),
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
