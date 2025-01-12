import { getProductQueryOptions } from "@/products/hooks/query-options/get-product-query-options";
import { useQuery } from "@tanstack/react-query";

export const useFetchProduct = (id: string) =>
  useQuery(getProductQueryOptions(id));
