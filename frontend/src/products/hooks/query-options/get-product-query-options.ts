import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { ProductDetailsFiltersDto } from "@/products/dtos/product-details-filters-dto";
import { ProductDetailEntity } from "@/products/entities/product-detail-entity";
import { productsQueryKey } from "@/products/hooks/query-options/products-query-key";
import { queryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchProduct = async (id: string, params: ProductDetailsFiltersDto) => {
  const { data } = await apiService.get<ProductDetailEntity>(
    `/api/v1/products/${id}`,
    {
      params,
    },
  );
  return data;
};

export const getProductQueryOptions = (
  id: string,
  params: ProductDetailsFiltersDto = { priceStampRangeDays: "7" } as const,
) =>
  queryOptions<ProductDetailEntity, AxiosError<ErrorResponse>>({
    queryKey: [...productsQueryKey, id, params] as const,
    queryFn: () => fetchProduct(id, params),
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
