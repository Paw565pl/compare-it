import { getCommentPageQueryOptions } from "@/comments/hooks/query-options/get-comment-page-query-options";
import { useInfiniteQuery } from "@tanstack/react-query";

export const useFetchCommentPage = (productId: string, accessToken?: string) =>
  useInfiniteQuery(getCommentPageQueryOptions(productId, accessToken));
