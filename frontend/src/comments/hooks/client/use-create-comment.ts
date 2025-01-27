import { CommentDto } from "@/comments/dtos/comment-dto";
import { CommentEntity } from "@/comments/entities/comment-entity";
import { commentsQueryKey } from "@/comments/hooks/query-options/comments-query-key";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { productsQueryKey } from "@/products/hooks/query-options/products-query-key";
import { useMutation } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

const createComment = async (
  accessToken: string,
  productId: string,
  commentDto: CommentDto,
) => {
  const { data } = await axios.post<CommentEntity>(
    `/v1/products/${productId}/comments`,
    commentDto,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const useCreateComment = (accessToken: string, productId: string) =>
  useMutation<CommentEntity, AxiosError<ErrorResponse>, CommentDto>({
    mutationKey: [...productsQueryKey, productId, "createComment"] as const,
    mutationFn: (commentDto) =>
      createComment(accessToken, productId, commentDto),
    onSuccess: () => {
      const queryClient = getQueryClient();
      const queryKey = [...productsQueryKey, productId, ...commentsQueryKey];

      queryClient.invalidateQueries({ queryKey });
    },
  });
