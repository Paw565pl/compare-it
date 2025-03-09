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
  paginationOptions?: PaginationOptions,
) => {
  const { data } = await apiService.get<PaginatedData<CommentEntity>>(
    `/api/v1/products/${productId}/comments`,
    {
      params: {
        page: pageParam,
        ...paginationOptions,
      },
    },
  );
  return data;
};

export const getCommentPageQueryOptions = (
  productId: string,
  paginationOptions?: PaginationOptions,
) =>
  infiniteQueryOptions<PaginatedData<CommentEntity>, AxiosError<ErrorResponse>>(
    {
      queryKey: [
        ...productsQueryKey,
        productId,
        ...commentsQueryKey,
        paginationOptions,
      ] as const,
      queryFn: ({ pageParam }) =>
        fetchCommentPage(productId, pageParam, paginationOptions),
      getNextPageParam: ({ page: { number, totalPages } }) =>
        number + 1 < totalPages ? number + 1 : undefined,
      initialPageParam: 0,
    },
  );
