import { getCommentQueryOptions } from "@/comments/hooks/query-options/get-comment-query-options";
import { useQuery } from "@tanstack/react-query";

export const useFetchComment = (
  productId: string,
  commentId: string,
  accessToken?: string,
) => useQuery(getCommentQueryOptions(productId, commentId, accessToken));
