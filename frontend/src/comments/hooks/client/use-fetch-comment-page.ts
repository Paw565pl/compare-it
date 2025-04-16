import { getCommentPageQueryOptions } from "@/comments/hooks/query-options/get-comment-page-query-options";
import { PaginationOptions } from "@/core/services/api/types/pagination-options";
import { useInfiniteQuery } from "@tanstack/react-query";

export const useFetchCommentPage = (
  productId: string,
  accessToken?: string,
  paginationOptions?: PaginationOptions,
) =>
  useInfiniteQuery(
    getCommentPageQueryOptions(productId, accessToken, paginationOptions),
  );
