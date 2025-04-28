import { ProductDetailsFiltersDto } from "@/products/dtos/product-details-filters-dto";
import { getProductQueryOptions } from "@/products/hooks/query-options/get-product-query-options";
import { useQuery } from "@tanstack/react-query";

export const useFetchProduct = (
  id: string,
  params?: ProductDetailsFiltersDto,
) => useQuery(getProductQueryOptions(id, params));
