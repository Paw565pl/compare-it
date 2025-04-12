import { getCommentPageQueryOptions } from "@/comments/hooks/query-options/get-comment-page-query-options";
import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchCommentPage = (
  queryClient: QueryClient,
  productId: string,
  accessToken?: string,
  paginationOptions?: PaginationOptions,
) =>
  queryClient.prefetchInfiniteQuery(
    getCommentPageQueryOptions(productId, accessToken, paginationOptions),
  );
