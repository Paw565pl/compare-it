import { getRatingQueryOptions } from "@/rating/hooks/query-options/get-rating-query-options";
import { useQuery } from "@tanstack/react-query";

export const useFetchRating = (
  accessToken: string,
  userId: string,
  productId: string,
  commentId: string,
) => useQuery(getRatingQueryOptions(accessToken, userId, productId, commentId));
