import { CommentEntity } from "@/comments/entities/comment-entity";
import { commentsQueryKey } from "@/comments/hooks/query-options/comments-query-key";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { productsQueryKey } from "@/products/hooks/query-options/products-query-key";
import { queryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchComment = async (
  productId: string,
  commentId: string,
  accessToken?: string,
) => {
  const { data } = await apiService.get<CommentEntity>(
    `/api/v1/products/${productId}/comments/${commentId}`,
    {
      headers: {
        Authorization: accessToken ? `Bearer ${accessToken}` : undefined,
      },
    },
  );
  return data;
};

export const getCommentQueryOptions = (
  productId: string,
  commentId: string,
  accessToken?: string,
) =>
  queryOptions<CommentEntity, AxiosError<ErrorResponse>>({
    // eslint-disable-next-line @tanstack/query/exhaustive-deps
    queryKey: [
      ...productsQueryKey,
      productId,
      ...commentsQueryKey,
      commentId,
    ] as const,
    queryFn: () => fetchComment(productId, commentId, accessToken),
  });
