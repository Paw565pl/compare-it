import { getCommentQueryOptions } from "@/comments/hooks/query-options/get-comment-query-options";
import { QueryClient } from "@tanstack/react-query";

export const prefetchComment = (
  queryClient: QueryClient,
  productId: string,
  commentId: string,
  accessToken?: string,
) =>
  queryClient.prefetchQuery(
    getCommentQueryOptions(productId, commentId, accessToken),
  );
