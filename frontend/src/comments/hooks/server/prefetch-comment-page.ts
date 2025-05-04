import { getCommentPageQueryOptions } from "@/comments/hooks/query-options/get-comment-page-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchCommentPage = (
  queryClient: QueryClient,
  productId: string,
  accessToken?: string,
) =>
  queryClient.prefetchInfiniteQuery(
    getCommentPageQueryOptions(productId, accessToken),
  );
