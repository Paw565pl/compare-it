import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { ProductDetailEntity } from "@/products/entities/product-detail-entity";
import { productsQueryKey } from "@/products/hooks/products-query-key";
import { queryOptions } from "@tanstack/react-query";
import { AxiosError } from "axios";

const fetchProduct = async (id: string) => {
  const { data } = await apiService.get<ProductDetailEntity>(
    `/v1/products/${id}`,
  );
  return data;
};

export const getProductQueryOptions = (id: string) =>
  queryOptions<ProductDetailEntity, AxiosError<ErrorResponse>>({
    queryKey: [...productsQueryKey, id] as const,
    queryFn: () => fetchProduct(id),
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
