import { getIsProductFavoriteQueryOptions } from "@/favorite-products/hooks/query-options/get-is-product-favorite-query-options";
import { useQuery } from "@tanstack/react-query";

export const useIsProductFavorite = (
  accessToken: string,
  productId: string,
  userId: string,
) => useQuery(getIsProductFavoriteQueryOptions(accessToken, productId, userId));
