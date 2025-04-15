import { CommentEntity } from "@/comments/entities/comment-entity";
import { commentsQueryKey } from "@/comments/hooks/query-options/comments-query-key";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { PaginatedData } from "@/core/services/api/types/paginated-data";
import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { productsQueryKey } from "@/products/hooks/query-options/products-query-key";
import { infiniteQueryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchCommentPage = async (
  productId: string,
  pageParam: unknown,
  accessToken?: string,
  paginationOptions?: PaginationOptions,
) => {
  const { data } = await apiService.get<PaginatedData<CommentEntity>>(
    `/api/v1/products/${productId}/comments`,
    {
      params: {
        ...paginationOptions,
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
  paginationOptions?: PaginationOptions,
) =>
  infiniteQueryOptions<PaginatedData<CommentEntity>, AxiosError<ErrorResponse>>(
    {
      // eslint-disable-next-line @tanstack/query/exhaustive-deps
      queryKey: [
        ...productsQueryKey,
        productId,
        ...commentsQueryKey,
        paginationOptions,
      ] as const,
      queryFn: ({ pageParam }) =>
        fetchCommentPage(productId, pageParam, accessToken, paginationOptions),
      getNextPageParam: ({ page: { number, totalPages } }) =>
        number + 1 < totalPages ? number + 1 : undefined,
      initialPageParam: 0,
    },
  );
