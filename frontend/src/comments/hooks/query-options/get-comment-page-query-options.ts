import { CommentEntity } from "@/comments/entities/comment-entity";
import { commentsQueryKey } from "@/comments/hooks/query-options/comments-query-key";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { PaginatedData } from "@/core/services/api/types/paginated-data";
import { productsQueryKey } from "@/products/hooks/query-options/products-query-key";
import { infiniteQueryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchCommentPage = async (
  productId: string,
  pageParam: unknown,
  accessToken?: string,
) => {
  const { data } = await apiService.get<PaginatedData<CommentEntity>>(
    `/api/v1/products/${productId}/comments`,
    {
      params: {
        page: pageParam,
      },
      headers: {
        Authorization: accessToken ? `Bearer ${accessToken}` : undefined,
      },
    },
  );
  return data;
};

export const getCommentPageQueryOptions = (
  productId: string,
  accessToken?: string,
) =>
  infiniteQueryOptions<PaginatedData<CommentEntity>, AxiosError<ErrorResponse>>(
    {
      // eslint-disable-next-line @tanstack/query/exhaustive-deps
      queryKey: [...productsQueryKey, productId, ...commentsQueryKey] as const,
      queryFn: ({ pageParam }) =>
        fetchCommentPage(productId, pageParam, accessToken),
      getNextPageParam: ({ page: { number, totalPages } }) =>
        number + 1 < totalPages ? number + 1 : undefined,
      initialPageParam: 0,
    },
  );
