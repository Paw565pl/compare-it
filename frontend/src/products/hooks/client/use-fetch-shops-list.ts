import { shopsListQueryOptions } from "@/products/hooks/query-options/shops-list-query-options";
import { useQuery } from "@tanstack/react-query";

export const useFetchShopsList = () => useQuery(shopsListQueryOptions);
